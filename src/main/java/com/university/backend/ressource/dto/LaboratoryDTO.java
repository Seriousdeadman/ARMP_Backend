package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.LabType;
import com.university.backend.ressource.enums.ResourceStatus;
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