package com.university.backend.hr.config;

import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.GradeName;
import com.university.backend.hr.repositories.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HrGradeBootstrap implements CommandLineRunner {

    private static final Map<GradeName, BigDecimal> BASE = new EnumMap<>(GradeName.class);

    static {
        BASE.put(GradeName.ASSISTANT, new BigDecimal("3000.00"));
        BASE.put(GradeName.MAITRE, new BigDecimal("4500.00"));
        BASE.put(GradeName.PROF, new BigDecimal("6000.00"));
    }

    private final GradeRepository gradeRepository;

    @Override
    public void run(String... args) {
        for (GradeName name : GradeName.values()) {
            if (gradeRepository.findByName(name).isEmpty()) {
                BigDecimal base = BASE.getOrDefault(name, new BigDecimal("3000.00"));
                gradeRepository.save(Grade.builder()
                        .name(name)
                        .baseSalary(base)
                        .hourlyBonus(new BigDecimal("0.00"))
                        .build());
            }
        }
    }
}
