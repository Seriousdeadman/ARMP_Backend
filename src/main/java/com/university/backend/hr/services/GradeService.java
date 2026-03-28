package com.university.backend.hr.services;

import com.university.backend.hr.dto.GradeRequest;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.repositories.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;

    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    public Grade findById(String id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
    }

    @Transactional
    public Grade create(GradeRequest request) {
        Grade grade = Grade.builder()
                .name(request.getName())
                .baseSalary(request.getBaseSalary())
                .hourlyBonus(request.getHourlyBonus())
                .build();
        return gradeRepository.save(grade);
    }

    @Transactional
    public Grade update(String id, GradeRequest request) {
        Grade grade = findById(id);
        grade.setName(request.getName());
        grade.setBaseSalary(request.getBaseSalary());
        grade.setHourlyBonus(request.getHourlyBonus());
        return gradeRepository.save(grade);
    }

    @Transactional
    public void delete(String id) {
        if (!gradeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found");
        }
        gradeRepository.deleteById(id);
    }
}
