package com.university.backend.dto;

import com.university.backend.enums.ResourceStatus;
import com.university.backend.enums.SpaceType;
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