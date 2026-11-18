package com.EstateFlowCRM.platform.security.dto.response;

import com.EstateFlowCRM.platform.user.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    // سؤال : متى ارجع ال  id و متى لا
    private String accessToken;
    private String tokenType;
    private String fullName;
    private String email;
    private Role role;
    private boolean mustChangePassword;
}
