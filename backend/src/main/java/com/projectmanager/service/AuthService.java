package com.projectmanager.service;

import com.projectmanager.dto.AuthResponse;
import com.projectmanager.dto.LoginRequest;
import com.projectmanager.dto.RegisterRequest;
import com.projectmanager.dto.UserDto;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    UserDto getCurrentUser(Long userId);
}

