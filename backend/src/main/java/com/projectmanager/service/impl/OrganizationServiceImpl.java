package com.projectmanager.service.impl;

import com.projectmanager.dto.OrganizationRequest;
import com.projectmanager.dto.OrganizationResponse;
import com.projectmanager.entity.Organization;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.OrganizationMapper;
import com.projectmanager.repository.OrganizationRepository;
import com.projectmanager.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        Organization organization = Organization.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Organization saved = organizationRepository.save(organization);
        log.info("Created organization: {} (id: {})", saved.getName(), saved.getId());
        return organizationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        return organizationMapper.toDto(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(organizationMapper::toDto)
                .collect(Collectors.toList());
    }
}
