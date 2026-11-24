package com.EstateFlowCRM.platform.security.controllers;

import com.EstateFlowCRM.commons.ErrorResponse.GlobalExceptionHandler;
import com.EstateFlowCRM.platform.security.dto.request.LoginRequest;
import com.EstateFlowCRM.platform.security.dto.request.RegisterRequest;
import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import com.EstateFlowCRM.platform.security.exceptions.InvalidCredentialsException;
import com.EstateFlowCRM.platform.security.exceptions.RoleAssignmentNotAllowedException;
import com.EstateFlowCRM.platform.security.services.AuthService;
import com.EstateFlowCRM.platform.user.entities.Role;
import com.EstateFlowCRM.platform.user.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerApiTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void loginReturnsTokenAndUserDetails() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(LoginResponse.builder()
                .accessToken("test-access-token")
                .tokenType("Bearer")
                .fullName("Test Agent")
                .email("agent@example.com")
                .role(Role.SALES_AGENT)
                .mustChangePassword(true)
                .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"agent@example.com","password":"correct-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test-access-token")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SALES_AGENT")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("agent@example.com")));
    }

    @Test
    void loginMapsInvalidCredentialsToUnauthorizedError() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"agent@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"status\":401")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("INVALID_CREDENTIALS")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid email or password")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/auth/login")));
    }

    @Test
    void registerReturnsCreatedAndCreatedUser() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(RegisterResponse.builder()
                .userId(UUID.fromString("7f34c7d6-fca1-4a75-bf45-116eae42b2a8"))
                .fullName("Test Agent")
                .email("agent@example.com")
                .role(Role.SALES_AGENT)
                .build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Test Agent","email":"agent@example.com","password":"long-test-password","role":"SALES_AGENT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("agent@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SALES_AGENT")));
    }

    @Test
    void registerMapsDuplicateEmailToConflictError() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new UserAlreadyExistsException());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration("SALES_AGENT")))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("USER_ALREADY_EXISTS")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User with this email already exists")));
    }

    @Test
    void registerMapsForbiddenRoleToBadRequestError() throws Exception {
        doThrow(new RoleAssignmentNotAllowedException())
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration("OVERVIEW_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ROLE_ASSIGNMENT_NOT_ALLOWED")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This role cannot be assigned through registration")));
    }

    @Test
    void invalidRegistrationFieldsReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":" ","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("VALIDATION_FAILED")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Full name is required")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Role is required")));
    }

    @Test
    void malformedJsonReturnsSafeClientError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MALFORMED_REQUEST")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Request body is missing or malformed")));
    }

    @Test
    void unexpectedServerFailureDoesNotExposeExceptionDetails() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalStateException("sensitive internal detail"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"agent@example.com","password":"correct-password"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("INTERNAL_ERROR")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("An unexpected error occurred")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive internal detail"))));
    }

    private String validRegistration(String role) {
        return """
                {"fullName":"Test Agent","email":"agent@example.com","password":"long-test-password","role":"%s"}
                """.formatted(role);
    }
}
