package com.slow3586.highload_0224_skv.mainapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Анкета не найдена")
public class UserNotFoundException extends RuntimeException {
}
