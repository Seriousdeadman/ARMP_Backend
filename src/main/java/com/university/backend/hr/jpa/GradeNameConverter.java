package com.university.backend.hr.jpa;

import com.university.backend.hr.enums.GradeName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GradeNameConverter implements AttributeConverter<GradeName, String> {

    @Override
    public String convertToDatabaseColumn(GradeName attribute) {
        return attribute == null ? GradeName.ASSISTANT.name() : attribute.name();
    }

    @Override
    public GradeName convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return GradeName.ASSISTANT;
        }
        try {
            return GradeName.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            return GradeName.ASSISTANT;
        }
    }
}
