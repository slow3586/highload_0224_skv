package com.slow3586.highload_0224_skv.mainapp.service.websocketserver;

import com.slow3586.highload_0224_skv.mainapp.security.JwtService;
import com.slow3586.highload_0224_skv.mainapp.service.FriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class WebSocketServerApi {
    FriendService friendService;
    JwtService jwtService;

    @PostMapping(path = "/getUser")
    public String getUser(@RequestBody String jwt) {
        return jwtService.extractUserId(jwt).toString();
    }

    @GetMapping(path = "/checkIfUsersAreFriends")
    public boolean checkIfUsersAreFriends(
        @RequestParam(value = "user0") String user0,
        @RequestParam(value = "user1") String user1
    ) {
        return friendService.checkIfUsersAreFriends(
            UUID.fromString(user0),
            UUID.fromString(user1));
    }
}
