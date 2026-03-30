package com.university.backend.hr.jpa;

import com.university.backend.hr.enums.EmployeeStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmployeeStatusConverter implements AttributeConverter<EmployeeStatus, String> {

    @Override
    public String convertToDatabaseColumn(EmployeeStatus attribute) {
        return attribute == null ? EmployeeStatus.ACTIVE.name() : attribute.name();
    }

    @Override
    public EmployeeStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return EmployeeStatus.ACTIVE;
        }
        try {
            return EmployeeStatus.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            return EmployeeStatus.PENDING_VALIDATION;
        }
    }
}
