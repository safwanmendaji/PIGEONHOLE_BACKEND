package com.appbackend.example.AppBackend.models;


import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RegisterRequest {

    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phoneNumber;
    private int role;
    private Integer workId;
    private String dob;
    private String age;
    private String gender;
    private String address;
    private String maritalStatus;
    private String kin;
    private String kinNumber;
    private String kin1;
    private String kin1Number;
    private String nationalId;
    private boolean isDocumentDataSubmitted;
    private boolean isUserImageSubmitted;
    private boolean isDigitalSignatureSubmitted;



}
