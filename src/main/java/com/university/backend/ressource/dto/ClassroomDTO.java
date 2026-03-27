package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ClassroomType;
import com.university.backend.ressource.enums.ResourceStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDTO {
    private String name;
    private Integer capacity;
    private String building;
    private String roomNumber;
    private ClassroomType classroomType;
    private ResourceStatus status;
}