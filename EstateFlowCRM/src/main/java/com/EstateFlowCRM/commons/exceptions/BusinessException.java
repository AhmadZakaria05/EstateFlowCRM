package com.EstateFlowCRM.commons.exceptions;

public abstract class BusinessException extends RuntimeException{


    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BusinessException(String message) {
        super(message);
    }


}
