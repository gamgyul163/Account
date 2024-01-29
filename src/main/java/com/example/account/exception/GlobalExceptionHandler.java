package com.example.account.exception;

import com.example.account.dto.ErroeResponse;
import com.example.account.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AccountException.class)
  public ErroeResponse handleAccountException(AccountException e) {
    log.error("AccountException {} is occurred", e.getErrorCode());
    return new ErroeResponse(e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ErroeResponse handleException(DataIntegrityViolationException e) {
    log.error("DataIntegrityViolationException is occurred", e);
    return new ErroeResponse(ErrorCode.INVALID_REQUEST,
        ErrorCode.INVALID_REQUEST.getDescription());
  }

  @ExceptionHandler(Exception.class)
  public ErroeResponse handleException(Exception e) {
    log.error("Exception is occurred", e);
    return new ErroeResponse(ErrorCode.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
  }
}
