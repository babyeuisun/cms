package com.zerobase.cms.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionController {
    @ExceptionHandler({
            CustomerException.class
    })
    public ResponseEntity<ExceptionResponse> customRequestException(final CustomerException c){
        log.warn("api Exception: {}",c.getErrorCode());
        return ResponseEntity.badRequest().body(new ExceptionResponse(c.getMessage(),c.getErrorCode()));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ExceptionResponse{

        private String message;
        private ErrorCode errorCode;
    }

}
