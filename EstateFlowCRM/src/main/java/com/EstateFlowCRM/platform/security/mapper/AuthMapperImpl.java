package com.EstateFlowCRM.platform.security.mapper;

import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import com.EstateFlowCRM.platform.user.entities.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapperImpl implements AuthMapper  {

    @Override
    public LoginResponse toLoginResponse(User user, String accessToken) {

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    @Override
    public RegisterResponse toRegisterResponse(User user) {

        return RegisterResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
