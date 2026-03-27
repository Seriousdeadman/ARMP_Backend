package com.university.backend.dto;

import com.university.backend.enums.EquipmentType;
import com.university.backend.enums.ResourceStatus;
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