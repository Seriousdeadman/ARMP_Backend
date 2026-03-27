package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.EquipmentType;
import com.university.backend.ressource.enums.ResourceStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDTO {
    private String name;
    private String brand;
    private String model;
    private EquipmentType equipmentType;
    private ResourceStatus status;
}