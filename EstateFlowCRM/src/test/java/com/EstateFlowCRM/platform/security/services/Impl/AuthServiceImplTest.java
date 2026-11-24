package com.EstateFlowCRM.platform.security.services.Impl;

import com.EstateFlowCRM.platform.security.dto.request.LoginRequest;
import com.EstateFlowCRM.platform.security.dto.request.RegisterRequest;
import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import com.EstateFlowCRM.platform.security.exceptions.InvalidCredentialsException;
import com.EstateFlowCRM.platform.security.exceptions.RoleAssignmentNotAllowedException;
import com.EstateFlowCRM.platform.security.mapper.AuthMapper;
import com.EstateFlowCRM.platform.security.services.JwtService;
import com.EstateFlowCRM.platform.user.entities.Role;
import com.EstateFlowCRM.platform.user.entities.User;
import com.EstateFlowCRM.platform.user.exceptions.UserAlreadyExistsException;
import com.EstateFlowCRM.platform.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthMapper authMapper;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authMapper = mock(AuthMapper.class);
        authService = new AuthServiceImpl(
                userRepository, authenticationManager, passwordEncoder, jwtService, authMapper
        );
    }

    @Test
    void loginNormalizesEmailAndReturnsMappedResponse() {
        User user = user("agent@example.com", Role.SALES_AGENT);
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("agent@example.com")
                .password("encoded")
                .authorities("ROLE_SALES_AGENT")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        LoginResponse expected = LoginResponse.builder().accessToken("token").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("agent@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(principal)).thenReturn("token");
        when(authMapper.toLoginResponse(user, "token")).thenReturn(expected);

        LoginResponse actual = authService.login(LoginRequest.builder()
                .email("  Agent@Example.com ")
                .password("password")
                .build());

        assertSame(expected, actual);
        verify(userRepository).findByEmail("agent@example.com");
        verify(jwtService).generateToken(principal);
    }

    @Test
    void loginConvertsAuthenticationFailureToBusinessException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("credentials rejected"));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(LoginRequest.builder()
                        .email("agent@example.com")
                        .password("wrong")
                        .build())
        );

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void registerTrimsNameNormalizesEmailAndStoresOnlyEncodedPassword() {
        when(userRepository.existsByEmail("agent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("long-test-password")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RegisterResponse expected = RegisterResponse.builder().email("agent@example.com").build();
        when(authMapper.toRegisterResponse(any(User.class))).thenReturn(expected);

        RegisterResponse actual = authService.register(RegisterRequest.builder()
                .fullName("  Test Agent  ")
                .email(" Agent@Example.com ")
                .password("long-test-password")
                .role(Role.SALES_AGENT)
                .build());

        assertSame(expected, actual);
        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("Test Agent", savedUser.getFullName());
        assertEquals("agent@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.SALES_AGENT, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.isMustChangePassword());
        verify(passwordEncoder).encode("long-test-password");
    }

    @Test
    void registerRejectsOverviewAdminRole() {
        RoleAssignmentNotAllowedException exception = assertThrows(
                RoleAssignmentNotAllowedException.class,
                () -> authService.register(registration(Role.OVERVIEW_ADMIN))
        );

        assertEquals("ROLE_ASSIGNMENT_NOT_ALLOWED", exception.getCode());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registerRejectsEmailAlreadyPresentBeforeSaving() {
        when(userRepository.existsByEmail("agent@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registration(Role.SALES_AGENT))
        );

        assertEquals("USER_ALREADY_EXISTS", exception.getCode());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void registerMapsConcurrentEmailUniqueViolationToConflictException() {
        when(userRepository.existsByEmail("agent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("long-test-password")).thenReturn("encoded-password");
        SQLException sqlException = new SQLException(
                "duplicate key value violates unique constraint: Key (email)=(agent@example.com) already exists",
                "23505"
        );
        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException("duplicate key", sqlException);
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(databaseException);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registration(Role.SALES_AGENT))
        );

        assertEquals("USER_ALREADY_EXISTS", exception.getCode());
        assertSame(databaseException, exception.getCause());
    }

    @Test
    void registerPreservesUnrelatedDatabaseIntegrityErrors() {
        when(userRepository.existsByEmail("agent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("long-test-password")).thenReturn("encoded-password");
        DataIntegrityViolationException databaseException = new DataIntegrityViolationException(
                "unrelated database constraint",
                new SQLException("Key (full_name) already exists", "23505")
        );
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(databaseException);

        assertSame(databaseException, assertThrows(
                DataIntegrityViolationException.class,
                () -> authService.register(registration(Role.SALES_AGENT))
        ));
    }

    @Test
    void registerAllowsBothAssignableSalesRoles() {
        for (Role role : new Role[]{Role.SALES_AGENT, Role.SALES_MANAGER}) {
            when(userRepository.existsByEmail("agent@example.com")).thenReturn(false);
            when(passwordEncoder.encode("long-test-password")).thenReturn("encoded-password");
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(authMapper.toRegisterResponse(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                return RegisterResponse.builder().role(saved.getRole()).build();
            });

            RegisterResponse response = authService.register(registration(role));

            assertEquals(role, response.getRole());
        }

        verify(userRepository, org.mockito.Mockito.times(2)).saveAndFlush(any(User.class));
    }

    private RegisterRequest registration(Role role) {
        return RegisterRequest.builder()
                .fullName("Test Agent")
                .email("agent@example.com")
                .password("long-test-password")
                .role(role)
                .build();
    }

    private User user(String email, Role role) {
        return User.builder()
                .fullName("Test User")
                .email(email)
                .password("encoded")
                .role(role)
                .enabled(true)
                .mustChangePassword(false)
                .build();
    }
}
