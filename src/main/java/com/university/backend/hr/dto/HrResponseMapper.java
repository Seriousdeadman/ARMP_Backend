package com.university.backend.hr.dto;

import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.entities.Interview;

public final class HrResponseMapper {

    private HrResponseMapper() {
    }

    public static DepartmentSummaryDto toDepartmentSummary(Department department) {
        if (department == null) {
            return null;
        }
        return DepartmentSummaryDto.builder()
                .id(department.getId())
                .name(department.getName())
                .build();
    }

    public static GradeSummaryDto toGradeSummary(Grade grade) {
        if (grade == null) {
            return null;
        }
        return GradeSummaryDto.builder()
                .id(grade.getId())
                .name(grade.getName())
                .baseSalary(grade.getBaseSalary())
                .hourlyBonus(grade.getHourlyBonus())
                .build();
    }

    public static CvResponseDto toCvResponse(Cv cv) {
        if (cv == null) {
            return null;
        }
        return CvResponseDto.builder()
                .id(cv.getId())
                .skillsAndExperience(cv.getSkillsAndExperience())
                .fileName(cv.getFileName())
                .fileContentType(cv.getFileContentType())
                .fileSizeBytes(cv.getFileSizeBytes())
                .fileStoragePath(cv.getFileStoragePath())
                .build();
    }

    public static CandidateResponseDto toCandidateResponse(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        return CandidateResponseDto.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .status(candidate.getStatus())
                .department(toDepartmentSummary(candidate.getDepartment()))
                .cv(toCvResponse(candidate.getCv()))
                .build();
    }

    public static EmployeeResponseDto toEmployeeResponse(Employee employee) {
        return toEmployeeResponse(employee, null);
    }

    public static EmployeeResponseDto toEmployeeResponse(Employee employee, java.math.BigDecimal calculatedSalary) {
        if (employee == null) {
            return null;
        }
        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .hireDate(employee.getHireDate())
                .leaveBalance(employee.getLeaveBalance())
                .status(employee.getStatus())
                .grade(toGradeSummary(employee.getGrade()))
                .department(toDepartmentSummary(employee.getDepartment()))
                .calculatedSalary(calculatedSalary)
                .build();
    }

    public static InterviewResponseDto toInterviewResponse(Interview interview) {
        if (interview == null) {
            return null;
        }
        return InterviewResponseDto.builder()
                .id(interview.getId())
                .interviewDate(interview.getInterviewDate())
                .location(interview.getLocation())
                .score(interview.getScore())
                .status(interview.getStatus())
                .candidate(toCandidateResponse(interview.getCandidate()))
                .build();
    }
}
