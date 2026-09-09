package com.projectmanager.service;

import com.projectmanager.dto.AuthResponse;
import com.projectmanager.dto.LoginRequest;
import com.projectmanager.dto.RegisterRequest;
import com.projectmanager.dto.UserDto;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.security.JwtTokenProvider;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("hashedPassword123")
                .role(Role.DEVELOPER)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.DEVELOPER)
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user and return JWT")
    void shouldRegisterNewUserSuccessfully() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenFromUserId(eq(1L), eq("john@example.com"), eq("DEVELOPER")))
                .thenReturn("mocked.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getUser().getName()).isEqualTo("John Doe");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Should reject registration if email is already taken")
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldLoginSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = UserPrincipal.create(sampleUser);

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("mocked.jwt.token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getUser().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return current user details by ID")
    void shouldReturnCurrentUserDetails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserDto userDto = authService.getCurrentUser(1L);

        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    }
}

