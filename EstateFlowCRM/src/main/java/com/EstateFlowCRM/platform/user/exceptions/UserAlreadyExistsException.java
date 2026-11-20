package com.EstateFlowCRM.platform.user.exceptions;

import com.EstateFlowCRM.commons.exceptions.BusinessException;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistsException() {
        super("User with this email already exists");
    }
}
