package com.EstateFlowCRM.platform.security.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiSecurityExceptionHandlerTest {

    private final ApiSecurityExceptionHandler handler = new ApiSecurityExceptionHandler(
            JsonMapper.builder().build()
    );

    @Test
    void unauthenticatedResponseUsesStandardJsonErrorBody() throws Exception {
        MockHttpServletRequest request = request("/api/v1/auth/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new BadCredentialsException("missing token"));

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertTrue(response.getContentAsString().contains("\"code\":\"UNAUTHENTICATED\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/api/v1/auth/register\""));
        assertTrue(response.getContentAsString().contains("Authentication is required"));
    }

    @Test
    void accessDeniedResponseUsesStandardJsonErrorBody() throws Exception {
        MockHttpServletRequest request = request("/api/v1/auth/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("wrong role"));

        assertEquals(403, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertTrue(response.getContentAsString().contains("\"code\":\"ACCESS_DENIED\""));
        assertTrue(response.getContentAsString().contains("You do not have permission to access this resource"));
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRequestURI(path);
        return request;
    }
}
