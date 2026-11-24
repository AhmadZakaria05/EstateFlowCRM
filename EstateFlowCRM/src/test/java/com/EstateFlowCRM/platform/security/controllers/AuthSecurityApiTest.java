package com.EstateFlowCRM.platform.security.controllers;

import com.EstateFlowCRM.platform.security.config.SecurityConfig;
import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.security.ApiSecurityExceptionHandler;
import com.EstateFlowCRM.platform.security.security.JwtAuthenticationFilter;
import com.EstateFlowCRM.platform.security.services.AuthService;
import com.EstateFlowCRM.platform.security.services.JwtService;
import com.EstateFlowCRM.platform.user.entities.Role;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthSecurityApiTest.TestBeans.class})
class AuthSecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void loginIsPublic() throws Exception {
        when(authService.login(any())).thenReturn(LoginResponse.builder()
                .accessToken("test-access-token")
                .email("agent@example.com")
                .role(Role.SALES_AGENT)
                .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"agent@example.com","password":"correct-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test-access-token")));
    }

    @Test
    void registerWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration("SALES_AGENT")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("UNAUTHENTICATED")));
    }

    @Test
    void registerWithMalformedBearerTokenReturns401() throws Exception {
        when(jwtService.extractUsername("not-a-jwt"))
                .thenThrow(new MalformedJwtException("invalid token"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration("SALES_AGENT")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("UNAUTHENTICATED")));
    }

    @Test
    @WithMockUser(roles = "SALES_AGENT")
    void registerAsSalesAgentReturns403() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration("SALES_AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ACCESS_DENIED")));
    }

    @Test
    @WithMockUser(roles = "OVERVIEW_ADMIN")
    void registerAsOverviewAdminIsAllowedBySecurityAndCreatesSalesAgent() throws Exception {
        when(authService.register(any())).thenReturn(RegisterResponse.builder()
                .userId(UUID.fromString("7f34c7d6-fca1-4a75-bf45-116eae42b2a8"))
                .fullName("Test Agent")
                .email("agent@example.com")
                .role(Role.SALES_AGENT)
                .build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration("SALES_AGENT")))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SALES_AGENT")));
    }

    private String validRegistration(String role) {
        return """
                {"fullName":"Test Agent","email":"agent@example.com","password":"long-test-password","role":"%s"}
                """.formatted(role);
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean
        UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(
                JwtService jwtService,
                UserDetailsService userDetailsService
        ) {
            return new JwtAuthenticationFilter(jwtService, userDetailsService);
        }

        @Bean
        ApiSecurityExceptionHandler apiSecurityExceptionHandler() {
            return new ApiSecurityExceptionHandler(JsonMapper.builder().build());
        }
    }
}
