package com.EstateFlowCRM.platform.security.services;

import com.EstateFlowCRM.platform.security.dto.request.LoginRequest;
import com.EstateFlowCRM.platform.security.dto.request.RegisterRequest;
import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
    RegisterResponse register(RegisterRequest registerRequest);
}
