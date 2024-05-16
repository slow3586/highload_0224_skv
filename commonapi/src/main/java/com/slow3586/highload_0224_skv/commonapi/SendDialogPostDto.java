package com.slow3586.highload_0224_skv.commonapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendDialogPostDto {
    UUID authorUserId;
    UUID receiverUserId;
    String text;
}
