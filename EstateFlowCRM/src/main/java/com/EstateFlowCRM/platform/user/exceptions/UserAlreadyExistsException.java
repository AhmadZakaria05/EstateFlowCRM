package com.EstateFlowCRM.platform.user.exceptions;

import com.EstateFlowCRM.commons.exceptions.BusinessException;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super("USER_ALREADY_EXISTS", message, cause);
    }

    public UserAlreadyExistsException() {
        super("USER_ALREADY_EXISTS", "User with this email already exists");
    }
}
