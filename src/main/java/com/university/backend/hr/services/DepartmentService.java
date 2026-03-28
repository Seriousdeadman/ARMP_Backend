package com.university.backend.hr.services;

import com.university.backend.hr.dto.DepartmentRequest;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    @Transactional
    public Department create(DepartmentRequest request) {
        Department department = Department.builder()
                .name(request.getName())
                .build();
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(String id, DepartmentRequest request) {
        Department department = findById(id);
        department.setName(request.getName());
        return departmentRepository.save(department);
    }

    @Transactional
    public void delete(String id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }
        departmentRepository.deleteById(id);
    }
}
