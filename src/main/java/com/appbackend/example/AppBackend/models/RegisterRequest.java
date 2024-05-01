package com.appbackend.example.AppBackend.models;


import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
//@Component
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phoneNumber;
    private int role;
}
