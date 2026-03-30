package com.university.backend.hr.jpa;

import com.university.backend.hr.enums.InterviewStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InterviewStatusConverter implements AttributeConverter<InterviewStatus, String> {

    @Override
    public String convertToDatabaseColumn(InterviewStatus attribute) {
        return attribute == null ? InterviewStatus.PLANNED.name() : attribute.name();
    }

    @Override
    public InterviewStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return InterviewStatus.PLANNED;
        }
        try {
            return InterviewStatus.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            return InterviewStatus.CANCELED;
        }
    }
}
