package com.slow3586.highload_0224_skv.mainapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@Table("users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    UUID id;
    String firstName;
    String secondName;
    LocalDate birthdate;
    String biography;
    String city;
    String password;
}
