package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.enums.SpaceType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaborativeSpaceDTO {
    private String name;
    private Integer capacity;
    private String building;
    private String roomNumber;
    private SpaceType spaceType;
    private ResourceStatus status;
}