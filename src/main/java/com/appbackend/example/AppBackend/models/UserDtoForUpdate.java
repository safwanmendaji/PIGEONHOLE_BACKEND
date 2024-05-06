package com.appbackend.example.AppBackend.models;

import lombok.Data;

@Data
public class UserDtoForUpdate {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
}
