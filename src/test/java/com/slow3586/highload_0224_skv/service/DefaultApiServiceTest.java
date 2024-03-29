package com.slow3586.highload_0224_skv.service;

import com.slow3586.highload_0224_skv.api.model.LoginPost200Response;
import com.slow3586.highload_0224_skv.api.model.LoginPostRequest;
import com.slow3586.highload_0224_skv.api.model.User;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPost200Response;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPostRequest;
import com.slow3586.highload_0224_skv.entity.UserEntity;
import com.slow3586.highload_0224_skv.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DefaultApiServiceTest extends Mockito {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_PASSWORD = "USER_PASSWORD";
    @Mock UserRepository userRepository;
    @InjectMocks DefaultApiService defaultApiService;

    @Test
    void loginPost() {
        UserEntity userEntity = UserEntity.builder()
            .id(USER_ID)
            .password(DefaultApiService.bCryptPasswordEncoder.encode(USER_PASSWORD))
            .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        LoginPostRequest loginPostRequest = LoginPostRequest.builder()
            .id(USER_ID.toString())
            .password(USER_PASSWORD)
            .build();

        LoginPost200Response result = defaultApiService.loginPost(loginPostRequest).getBody();

        assertNotNull(result);
        assertFalse(result.getToken().isEmpty());
    }

    @Test
    void userRegisterPost() {
        UserRegisterPostRequest request = UserRegisterPostRequest.builder()
            .firstName("FIRST_NAME")
            .secondName("SECOND_NAME")
            .birthdate(LocalDate.of(2000, 1, 1))
            .city("CITY")
            .biography("BIOGRAPHY")
            .password(USER_PASSWORD)
            .build();

        when(userRepository.save(any())).then(a -> {
            UserEntity entity = ((UserEntity) a.getArguments()[0]);
            entity.setId(USER_ID);
            return entity;
        });

        UserRegisterPost200Response result = defaultApiService.userRegisterPost(request).getBody();

        assertNotNull(result);
        assertEquals(36, result.getUserId().length());
    }

    @Test
    void userGetIdGet() {
        UserEntity userEntity = UserEntity.builder()
            .id(USER_ID)
            .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        User result = defaultApiService.userGetIdGet(USER_ID.toString()).getBody();

        assertNotNull(result);
        assertEquals(USER_ID.toString(), result.getId());
    }
}