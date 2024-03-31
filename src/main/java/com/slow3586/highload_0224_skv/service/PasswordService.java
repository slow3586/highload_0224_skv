package com.slow3586.highload_0224_skv.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordService {
    static int strength = 4;
    static SecureRandom secureRandom = new SecureRandom(new byte[]{1, 2, 3});
    static BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength, secureRandom);

    public boolean matches(String raw, String encoded) {
        return bCryptPasswordEncoder.matches(raw, encoded);
    }

    public String encode(String password){
        return bCryptPasswordEncoder.encode(password);
    }
}
