package com.slow3586.highload_0224_skv.service;

import com.slow3586.highload_0224_skv.api.DefaultApiDelegate;
import com.slow3586.highload_0224_skv.api.model.LoginPost200Response;
import com.slow3586.highload_0224_skv.api.model.LoginPostRequest;
import com.slow3586.highload_0224_skv.api.model.Post;
import com.slow3586.highload_0224_skv.api.model.PostCreatePostRequest;
import com.slow3586.highload_0224_skv.api.model.User;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPost200Response;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPostRequest;
import com.slow3586.highload_0224_skv.entity.UserEntity;
import com.slow3586.highload_0224_skv.exception.IncorrectLoginException;
import com.slow3586.highload_0224_skv.exception.IncorrectPasswordException;
import com.slow3586.highload_0224_skv.exception.UserNotFoundException;
import com.slow3586.highload_0224_skv.mapper.PostMapper;
import com.slow3586.highload_0224_skv.mapper.UserMapper;
import com.slow3586.highload_0224_skv.repository.read.UserReadRepository;
import com.slow3586.highload_0224_skv.repository.write.UserWriteRepository;
import com.slow3586.highload_0224_skv.security.JwtService;
import com.slow3586.highload_0224_skv.security.SecurityConfiguration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DefaultApiService implements DefaultApiDelegate {
    PasswordService passwordService;
    UserReadRepository userReadRepository;
    UserWriteRepository userWriteRepository;
    PostService postService;
    FriendService friendService;
    AuthenticationManager authenticationManager;
    SecurityConfiguration securityConfiguration;
    PostMapper postMapper;
    UserMapper userMapper;
    JwtService jwtService;
    NativeWebRequest nativeWebRequest;

    @Override
    public ResponseEntity<LoginPost200Response> loginPost(LoginPostRequest loginPostRequest) {
        Authentication authenticate = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginPostRequest.getId(),
                loginPostRequest.getPassword()));

        if (authenticate.isAuthenticated()) {
            var user = securityConfiguration.loadUserByUsername(loginPostRequest.getId());

            return ResponseEntity.ok(
                LoginPost200Response.builder()
                    .token(jwtService.generateToken(user))
                    .build());
        } else {
            throw new IncorrectPasswordException();
        }
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
            userMapper.userEntityToUser(
                this.findUser(id)));
    }

    @Override
    public ResponseEntity<List<User>> userSearchGet(String firstName, String lastName) {
        return ResponseEntity.ok(
            userReadRepository.searchAllByFirstNameContainingAndSecondNameContaining(
                    firstName,
                    lastName
                ).stream()
                .map(userMapper::userEntityToUser)
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

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.of(nativeWebRequest);
    }

    @Override
    public ResponseEntity<Void> friendSetUserIdPut(String userId) {
        friendService.createFriendship(getCurrentUserId(), UUID.fromString(userId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<Post>> postFeedGet(BigDecimal offset, BigDecimal limit) {
        if (offset == null || limit == null) throw new IllegalArgumentException("offset == null || limit == null");
        if (limit.intValue() <= 0 || limit.intValue() > 1000) throw new IllegalArgumentException("limit <=0 || limit > 1000");
        if (offset.intValue() < 0) throw new IllegalArgumentException("offset < 0");
        return ResponseEntity.ok(
            postService.findPostsByFriends(
                this.getCurrentUserId(),
                offset.intValue(),
                limit.intValue()));
    }

    @Override
    public ResponseEntity<String> postCreatePost(PostCreatePostRequest request) {
        return ResponseEntity.ok(
            postService.createPost(
                    getCurrentUserId(),
                    request.getText())
                .toString());
    }

    protected UUID getCurrentUserId() {
        return this.getRequest()
            .map(WebRequest::getUserPrincipal)
            .map(Principal::getName)
            .map(UUID::fromString)
            .orElseThrow();
    }
}
