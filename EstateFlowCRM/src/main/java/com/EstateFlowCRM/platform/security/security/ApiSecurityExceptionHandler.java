package com.EstateFlowCRM.platform.security.security;

import com.EstateFlowCRM.commons.ErrorResponse.ErrorResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ApiSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    //وظيفتها تحديد كيف يستجيب النظام عندما يحتاج الطلب إلى مصادقة، أو عندما تفشل مصادقة
    @Override
    public void commence( // from ApiSecurityExceptionHandler (From Spring Security)
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        writeError(request, response, HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED", "Authentication is required");
    }

    //وظيفتها تحديد كيف يستجيب النظام عندما يتم رفض الوصول بسبب عدم امتلاك الصلاحية المطلوبة.
    @Override
    public void handle( // // from AccessDeniedHandler (From Spring Security)
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        writeError(
                request, response,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this resource"
        );
    }

    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {

        response.setStatus(
                status.value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        jsonMapper.writeValue( // هاي الدالة بتعمل عمليتين:أولًا، تحول Java Object إلى JSON   .ثانيًا، تكتب الناتج داخل Response OutputStream.
                response.getOutputStream(),// بتعطيك stream تكتب فيها البينات
                new ErrorResponseDto(
                        Instant.now(),
                        status.value(),
                        code,
                        message,
                        request.getRequestURI()
        ));
    }
}
