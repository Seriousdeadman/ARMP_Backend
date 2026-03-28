package com.university.backend.hr.services;

import com.university.backend.entities.User;
import com.university.backend.enums.UserRole;
import com.university.backend.hr.dto.CvFileMetadataDto;
import com.university.backend.hr.dto.DepartmentSummaryDto;
import com.university.backend.hr.dto.portal.ApplicantApplicationResponse;
import com.university.backend.hr.dto.portal.ApplicantApplicationUpsertRequest;
import com.university.backend.hr.dto.portal.ApplicationStatusResponse;
import com.university.backend.hr.dto.portal.CreateLeaveRequestDto;
import com.university.backend.hr.dto.portal.LeavePreviewResponse;
import com.university.backend.hr.dto.portal.LeaveSummaryResponse;
import com.university.backend.hr.dto.portal.PortalLeaveRequestRow;
import com.university.backend.hr.dto.portal.SubmittedLeaveRequestResponse;
import com.university.backend.hr.support.LeaveDaysCalculator;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.CandidateStatus;
import com.university.backend.hr.enums.GradeName;
import com.university.backend.hr.enums.InterviewStatus;
import com.university.backend.hr.enums.LeaveRequestStatus;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.DepartmentRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
import com.university.backend.hr.repositories.InterviewRepository;
import com.university.backend.hr.repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HrPortalService {

    private static final DateTimeFormatter INTERVIEW_TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final int AUTO_PROVISION_LEAVE_BALANCE = 21;

    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;
    private final InterviewRepository interviewRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final CvFileStorageService cvFileStorageService;

    public ApplicationStatusResponse getApplicationStatus(User user) {
        Optional<Candidate> candidateOpt = candidateRepository.findByEmailIgnoreCase(user.getEmail());
        if (candidateOpt.isEmpty()) {
            return ApplicationStatusResponse.notLinked();
        }
        Candidate candidate = candidateOpt.get();
        CandidateStatus status = candidate.getStatus();
        if (status == CandidateStatus.NEW || status == CandidateStatus.INTERVIEWING) {
            List<Interview> planned = interviewRepository
                    .findByCandidate_IdAndStatusOrderByInterviewDateAsc(candidate.getId(), InterviewStatus.PLANNED);
            LocalDateTime now = LocalDateTime.now();
            Optional<Interview> next = planned.stream()
                    .filter(i -> i.getInterviewDate() != null && !i.getInterviewDate().isBefore(now))
                    .min(Comparator.comparing(Interview::getInterviewDate));
            if (next.isPresent()) {
                Interview iv = next.get();
                return ApplicationStatusResponse.builder()
                        .candidateFound(true)
                        .candidateStatus(status.name())
                        .message("Your interview is scheduled for "
                                + iv.getInterviewDate().format(INTERVIEW_TS)
                                + " at " + iv.getLocation() + ".")
                        .interviewScheduledAt(iv.getInterviewDate().format(INTERVIEW_TS))
                        .interviewLocation(iv.getLocation())
                        .build();
            }
            return ApplicationStatusResponse.builder()
                    .candidateFound(true)
                    .candidateStatus(status.name())
                    .message(status == CandidateStatus.INTERVIEWING
                            ? "You are in the interview stage."
                            : "Your application is under review.")
                    .build();
        }
        if (status == CandidateStatus.ACCEPTED) {
            return ApplicationStatusResponse.builder()
                    .candidateFound(true)
                    .candidateStatus(status.name())
                    .message("Congratulations! Your application has been accepted.")
                    .build();
        }
        if (status == CandidateStatus.REJECTED) {
            return ApplicationStatusResponse.builder()
                    .candidateFound(true)
                    .candidateStatus(status.name())
                    .message("Your application was not successful.")
                    .build();
        }
        return ApplicationStatusResponse.builder()
                .candidateFound(true)
                .candidateStatus(status.name())
                .message("Your application is under review.")
                .build();
    }

    public List<DepartmentSummaryDto> listCareerDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> DepartmentSummaryDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .build())
                .toList();
    }

    @Transactional
    public LeaveSummaryResponse getLeaveSummary(User user) {
        Optional<Employee> employeeOpt = findEmployeeOrAutoProvisionStaff(user);
        if (employeeOpt.isEmpty()) {
            return LeaveSummaryResponse.notLinked();
        }
        Employee employee = employeeOpt.get();
        return LeaveSummaryResponse.builder()
                .employeeFound(true)
                .displayName(employee.getName())
                .remainingLeaveDays(employee.getLeaveBalance())
                .build();
    }

    public ApplicantApplicationResponse getMyApplication(User user) {
        return candidateRepository.findByEmailIgnoreCase(user.getEmail())
                .map(this::toApplicantApplicationResponse)
                .orElse(null);
    }

    @Transactional
    public ApplicantApplicationResponse saveMyApplication(User user, ApplicantApplicationUpsertRequest dto) {
        Department department = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(user.getEmail())
                .orElseGet(() -> Candidate.builder()
                        .email(user.getEmail())
                        .status(CandidateStatus.NEW)
                        .build());

        candidate.setName(dto.name().trim());
        candidate.setPhone(dto.phone().trim());
        candidate.setDepartment(department);
        if (candidate.getStatus() == null) {
            candidate.setStatus(CandidateStatus.NEW);
        }

        Cv cv = candidate.getCv();
        if (cv == null) {
            cv = Cv.builder()
                    .candidate(candidate)
                    .skillsAndExperience(dto.skillsAndExperience().trim())
                    .build();
            candidate.setCv(cv);
        } else {
            cv.setSkillsAndExperience(dto.skillsAndExperience().trim());
        }

        Candidate saved = candidateRepository.save(candidate);
        return toApplicantApplicationResponse(saved);
    }

    @Transactional
    public CvFileMetadataDto uploadMyCvFile(User user, MultipartFile file) {
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submit your application first."));
        Cv cv = candidate.getCv();
        if (cv == null) {
            cv = Cv.builder()
                    .candidate(candidate)
                    .skillsAndExperience("No text CV provided yet.")
                    .build();
            candidate.setCv(cv);
        }
        String previousPath = cv.getFileStoragePath();
        CvFileStorageService.StoredCvFile stored = cvFileStorageService.store(candidate.getId(), file);
        cv.setFileName(stored.originalFileName());
        cv.setFileContentType(stored.contentType());
        cv.setFileSizeBytes(stored.sizeBytes());
        cv.setFileStoragePath(stored.absolutePath());
        Candidate saved = candidateRepository.save(candidate);
        if (previousPath != null && !previousPath.isBlank() && !previousPath.equals(stored.absolutePath())) {
            cvFileStorageService.deleteIfExists(previousPath);
        }
        return toCvFileMetadata(saved);
    }

    public byte[] readMyCvFile(User user) {
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        Cv cv = candidate.getCv();
        if (cv == null || cv.getFileStoragePath() == null || cv.getFileStoragePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file not found.");
        }
        return cvFileStorageService.read(cv.getFileStoragePath());
    }

    @Transactional
    public CvFileMetadataDto deleteMyCvFile(User user) {
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        Cv cv = candidate.getCv();
        if (cv == null || cv.getFileStoragePath() == null || cv.getFileStoragePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file not found.");
        }
        String oldPath = cv.getFileStoragePath();
        cv.setFileName(null);
        cv.setFileContentType(null);
        cv.setFileSizeBytes(null);
        cv.setFileStoragePath(null);
        Candidate saved = candidateRepository.save(candidate);
        cvFileStorageService.deleteIfExists(oldPath);
        return toCvFileMetadata(saved);
    }

    public CvFileMetadataDto getMyCvMetadata(User user) {
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(user.getEmail()).orElse(null);
        if (candidate == null || candidate.getCv() == null) {
            return CvFileMetadataDto.builder()
                    .candidateId(candidate != null ? candidate.getId() : null)
                    .fileName(null)
                    .contentType(null)
                    .sizeBytes(null)
                    .filePresent(false)
                    .build();
        }
        return toCvFileMetadata(candidate);
    }

    @Transactional
    public List<PortalLeaveRequestRow> listMyLeaveRequests(User user) {
        Employee employee = requireEmployeeForLeavePortal(user);
        return leaveRequestRepository.findByEmployee_IdOrderByStartDateDesc(employee.getId()).stream()
                .map(this::toPortalLeaveRow)
                .toList();
    }

    @Transactional
    public LeavePreviewResponse previewLeave(User user, LocalDate startDate, LocalDate endDate) {
        Employee employee = requireEmployeeForLeavePortal(user);
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }
        int requestedDays = LeaveDaysCalculator.inclusiveCalendarDays(startDate, endDate);
        int remaining = employee.getLeaveBalance() != null ? employee.getLeaveBalance() : 0;
        return LeavePreviewResponse.builder()
                .requestedDays(requestedDays)
                .currentRemainingDays(remaining)
                .remainingAfterApproval(remaining - requestedDays)
                .build();
    }

    @Transactional
    public SubmittedLeaveRequestResponse submitLeaveRequest(User user, CreateLeaveRequestDto dto) {
        Employee employee = requireEmployeeForLeavePortal(user);
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }
        int requestedDays = LeaveDaysCalculator.inclusiveCalendarDays(dto.startDate(), dto.endDate());
        String reason = dto.reason() == null || dto.reason().isBlank()
                ? null
                : dto.reason().trim();
        LeaveRequest request = LeaveRequest.builder()
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .type(dto.type())
                .status(LeaveRequestStatus.PENDING)
                .employee(employee)
                .requestedDays(requestedDays)
                .reason(reason)
                .build();
        LeaveRequest saved = leaveRequestRepository.save(request);
        return new SubmittedLeaveRequestResponse(
                saved.getId(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getType().name(),
                saved.getStatus().name()
        );
    }

    /**
     * Staff roles (teacher, logistics) get a minimal {@link Employee} on first portal use when HR has not
     * created one yet — same email as the account, ASSISTANT grade and first department in the DB.
     */
    private Optional<Employee> findEmployeeOrAutoProvisionStaff(User user) {
        Optional<Employee> existing = employeeRepository.findByEmailIgnoreCase(user.getEmail());
        if (existing.isPresent()) {
            return existing;
        }
        if (user.getRole() != UserRole.TEACHER && user.getRole() != UserRole.LOGISTICS_STAFF) {
            return Optional.empty();
        }
        Grade grade = gradeRepository.findByName(GradeName.ASSISTANT).orElse(null);
        if (grade == null) {
            return Optional.empty();
        }
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            return Optional.empty();
        }
        String name = (user.getFirstName() + " " + user.getLastName()).trim();
        if (name.isBlank()) {
            name = user.getEmail();
        }
        Employee created = Employee.builder()
                .name(name)
                .email(user.getEmail())
                .hireDate(LocalDate.now())
                .leaveBalance(AUTO_PROVISION_LEAVE_BALANCE)
                .grade(grade)
                .department(departments.get(0))
                .build();
        return Optional.of(employeeRepository.save(created));
    }

    private Employee requireEmployeeForLeavePortal(User user) {
        return findEmployeeOrAutoProvisionStaff(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No HR employee record is linked to your account."
                ));
    }

    private PortalLeaveRequestRow toPortalLeaveRow(LeaveRequest lr) {
        return new PortalLeaveRequestRow(
                lr.getId(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getType().name(),
                lr.getStatus().name(),
                lr.getRequestedDays(),
                lr.getReason(),
                lr.getStatusMessage()
        );
    }

    private ApplicantApplicationResponse toApplicantApplicationResponse(Candidate candidate) {
        String skills = candidate.getCv() != null ? candidate.getCv().getSkillsAndExperience() : "";
        boolean hasFile = candidate.getCv() != null
                && candidate.getCv().getFileStoragePath() != null
                && !candidate.getCv().getFileStoragePath().isBlank();
        return ApplicantApplicationResponse.builder()
                .candidateId(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .status(candidate.getStatus())
                .departmentId(candidate.getDepartment().getId())
                .departmentName(candidate.getDepartment().getName())
                .skillsAndExperience(skills)
                .hasCvFile(hasFile)
                .build();
    }

    private CvFileMetadataDto toCvFileMetadata(Candidate candidate) {
        Cv cv = candidate.getCv();
        return CvFileMetadataDto.builder()
                .candidateId(candidate.getId())
                .fileName(cv != null ? cv.getFileName() : null)
                .contentType(cv != null ? cv.getFileContentType() : null)
                .sizeBytes(cv != null ? cv.getFileSizeBytes() : null)
                .filePresent(cv != null && cv.getFileStoragePath() != null && !cv.getFileStoragePath().isBlank())
                .build();
    }
}
