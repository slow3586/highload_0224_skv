package com.slow3586.highload_0224_skv.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordService {
    PasswordEncoder passwordEncoder;

    public String encode(String password){
        return passwordEncoder.encode(password);
    }
}
