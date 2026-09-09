package com.projectmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Root", description = "Root health and API info endpoints")
public class RootController {

    @GetMapping({"/", "/api"})
    @Operation(summary = "API Health and Info", description = "Returns service health and documentation links")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "Enterprise Issue & Project Management Platform",
                "version", "1.0.0",
                "documentation", "/swagger-ui/index.html",
                "apiDocs", "/api-docs"
        ));
    }
}

