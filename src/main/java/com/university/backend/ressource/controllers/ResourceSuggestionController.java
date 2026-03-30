// com.university.backend.ressource.controllers.ResourceSuggestionController.java
package com.university.backend.ressource.controllers;

import com.university.backend.ressource.dto.ResourceSuggestionRequest;
import com.university.backend.ressource.dto.ResourceSuggestionResponse;
import com.university.backend.ressource.services.ResourceSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResourceSuggestionController {

    private final ResourceSuggestionService suggestionService;

    @PostMapping("/resources")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSuggestionResponse>> suggestResources(
            @RequestBody ResourceSuggestionRequest request) {
        return ResponseEntity.ok(suggestionService.suggestResources(request));
    }
}