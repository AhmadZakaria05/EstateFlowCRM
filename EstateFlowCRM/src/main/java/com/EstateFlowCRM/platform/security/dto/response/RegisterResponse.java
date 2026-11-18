package com.EstateFlowCRM.platform.security.dto.response;

import com.EstateFlowCRM.platform.user.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private Role role;
}
