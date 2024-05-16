package com.slow3586.highload_0224_skv.mainapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Невалидные данные")
public class IncorrectLoginException extends RuntimeException {
}
