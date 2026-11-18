package com.EstateFlowCRM.platform.security.mapper;

import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import com.EstateFlowCRM.platform.user.entities.User;
import org.springframework.stereotype.Component;

@Component
public interface AuthMapper {

    LoginResponse toLoginResponse(User user, String accessToken);
    RegisterResponse toRegisterResponse(User user);
}
