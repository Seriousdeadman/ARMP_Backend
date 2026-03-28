package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.university.backend.hr.enums.LeaveRequestStatus;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {

    List<LeaveRequest> findByEmployee_Id(String employeeId);

    List<LeaveRequest> findByEmployee_IdOrderByStartDateDesc(String employeeId);

    List<LeaveRequest> findByStatusOrderByStartDateAsc(LeaveRequestStatus status);
}
