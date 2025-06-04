package com.stocktracker.stockinformation.config;

import com.stocktracker.stockinformation.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalStateException(IllegalStateException ex) {
        log.info("handler: Illegal State Exception: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleClientErrorException(HttpClientErrorException ex) {
        log.info("handler: Client Error Exception: {}", ex.getMessage());
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        return ResponseEntity.status(status).body(ex.getMessage());
    }

    @ExceptionHandler({ApiException.class, HttpServerErrorException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleApiException(Exception ex) {
        log.info("handler: Api Exception: {}", ex.getMessage());
        return ex.getMessage();
    }

}
