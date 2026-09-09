package com.projectmanager.service;

import com.projectmanager.dto.OrganizationRequest;
import com.projectmanager.dto.OrganizationResponse;

import java.util.List;

public interface OrganizationService {
    OrganizationResponse createOrganization(OrganizationRequest request);
    OrganizationResponse getOrganizationById(Long id);
    List<OrganizationResponse> getAllOrganizations();
}

