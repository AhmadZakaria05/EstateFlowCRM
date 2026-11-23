package com.EstateFlowCRM.platform.security.exceptions;

import com.EstateFlowCRM.commons.exceptions.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid email or password");
    }
}
