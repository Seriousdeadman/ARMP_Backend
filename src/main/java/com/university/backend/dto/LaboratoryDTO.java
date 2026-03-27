package com.university.backend.dto;

import com.university.backend.enums.LabType;
import com.university.backend.enums.ResourceStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryDTO {
    private String name;
    private Integer capacity;
    private String building;
    private String roomNumber;
    private LabType labType;
    private ResourceStatus status;
}