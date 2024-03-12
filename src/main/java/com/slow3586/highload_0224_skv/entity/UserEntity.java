package com.slow3586.highload_0224_skv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@Table("users")
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
