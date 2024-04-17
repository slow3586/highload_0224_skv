package com.slow3586.highload_0224_skv.service;

import com.slow3586.highload_0224_skv.api.DefaultApiDelegate;
import com.slow3586.highload_0224_skv.api.model.LoginPost200Response;
import com.slow3586.highload_0224_skv.api.model.LoginPostRequest;
import com.slow3586.highload_0224_skv.api.model.User;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPost200Response;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPostRequest;
import com.slow3586.highload_0224_skv.entity.UserEntity;
import com.slow3586.highload_0224_skv.exception.IncorrectLoginException;
import com.slow3586.highload_0224_skv.exception.IncorrectPasswordException;
import com.slow3586.highload_0224_skv.exception.UserNotFoundException;
import com.slow3586.highload_0224_skv.repository.read.UserReadRepository;
import com.slow3586.highload_0224_skv.repository.write.UserWriteRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DefaultApiService implements DefaultApiDelegate {

    PasswordService passwordService;
    UserReadRepository userReadRepository;
    UserWriteRepository userWriteRepository;

    @Override
    public ResponseEntity<LoginPost200Response> loginPost(LoginPostRequest loginPostRequest) {
        final UserEntity userEntity = this.findUser(loginPostRequest.getId());

        if (!passwordService.matches(
            loginPostRequest.getPassword(),
            userEntity.getPassword())
        ) {
            throw new IncorrectPasswordException();
        }

        return ResponseEntity.ok(
            LoginPost200Response.builder()
                .token((Jwts.builder()
                    .setSubject(userEntity.getId().toString())
                    .setIssuer("ISSUER")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + Duration.ofDays(1).toMillis()))
                    .signWith(SignatureAlgorithm.HS512, "SECRET_KEY")
                    .compact()))
                .build());
    }

    @Override
    public ResponseEntity<UserRegisterPost200Response> userRegisterPost(final UserRegisterPostRequest request) {
        final UserEntity userEntity = UserEntity.builder()
            .firstName(request.getFirstName())
            .secondName(request.getSecondName())
            .biography(request.getBiography())
            .birthdate(request.getBirthdate())
            .city(request.getCity())
            .password(passwordService.encode(request.getPassword()))
            .build();

        final UserEntity save = userWriteRepository.save(userEntity);

        return ResponseEntity.ok(UserRegisterPost200Response.builder()
            .userId(save.getId().toString())
            .build());
    }

    @Override
    public ResponseEntity<User> userGetIdGet(final String id) {
        return ResponseEntity.ok(
            this.userEntityToUser(
                this.findUser(id)));
    }

    @Override
    public ResponseEntity<List<User>> userSearchGet(String firstName, String lastName) {
        return ResponseEntity.ok(
            userReadRepository.searchAllByFirstNameContainingAndSecondNameContaining(
                    firstName,
                    lastName
                ).stream()
                .map(this::userEntityToUser)
                .toList());
    }

    protected UserEntity findUser(final String uuidString) {
        UUID uuid;

        try {
            uuid = UUID.fromString(uuidString);
        } catch (Exception e) {
            throw new IncorrectLoginException();
        }

        return userReadRepository.findById(uuid)
            .orElseThrow(UserNotFoundException::new);
    }

    protected User userEntityToUser(UserEntity userEntity) {
        return User.builder()
            .id(String.valueOf(userEntity.getId()))
            .firstName(userEntity.getFirstName())
            .secondName(userEntity.getSecondName())
            .biography(userEntity.getBiography())
            .birthdate(userEntity.getBirthdate())
            .city(userEntity.getCity())
            .build();
    }
}
