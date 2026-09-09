package com.projectmanager.service.impl;

import com.projectmanager.dto.AuthResponse;
import com.projectmanager.dto.LoginRequest;
import com.projectmanager.dto.RegisterRequest;
import com.projectmanager.dto.UserDto;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.security.JwtTokenProvider;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        String normalizedEmail = registerRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with email " + normalizedEmail + " already exists");
        }

        Role assignedRole = registerRequest.getRole() != null ? registerRequest.getRole() : Role.DEVELOPER;

        User user = User.builder()
                .name(registerRequest.getName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .role(assignedRole)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        String jwt = tokenProvider.generateTokenFromUserId(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(userMapper.toDto(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, loginRequest.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in successfully: {}", normalizedEmail);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toDto(user);
    }
}
