package com.EstateFlowCRM.platform.security.exceptions;

import com.EstateFlowCRM.commons.exceptions.BusinessException;

public class RoleAssignmentNotAllowedException extends BusinessException {

    public RoleAssignmentNotAllowedException() {
        super("ROLE_ASSIGNMENT_NOT_ALLOWED", "This role cannot be assigned through registration");
    }
}
