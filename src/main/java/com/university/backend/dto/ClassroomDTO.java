package com.university.backend.dto;

import com.university.backend.enums.ClassroomType;
import com.university.backend.enums.ResourceStatus;
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