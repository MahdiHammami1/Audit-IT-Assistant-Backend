package com.pwc.auditit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MicrosoftGraphException extends RuntimeException {

    private final HttpStatus status;

    public MicrosoftGraphException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public MicrosoftGraphException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
