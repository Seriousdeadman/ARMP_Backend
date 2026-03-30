package com.university.backend.hr.jpa;

import com.university.backend.hr.enums.CandidateStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CandidateStatusConverter implements AttributeConverter<CandidateStatus, String> {

    @Override
    public String convertToDatabaseColumn(CandidateStatus attribute) {
        return attribute == null ? CandidateStatus.NEW.name() : attribute.name();
    }

    @Override
    public CandidateStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return CandidateStatus.NEW;
        }
        String v = dbData.trim();
        if ("PENDING".equalsIgnoreCase(v)) {
            return CandidateStatus.NEW;
        }
        try {
            return CandidateStatus.valueOf(v);
        } catch (IllegalArgumentException ex) {
            return CandidateStatus.NEW;
        }
    }
}
