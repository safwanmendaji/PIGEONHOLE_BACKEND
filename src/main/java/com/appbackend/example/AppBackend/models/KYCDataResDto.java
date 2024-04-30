package com.appbackend.example.AppBackend.models;


import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class KYCDataResDto {
    private Integer workId;
    //    firstname will be avaialble from current authentication principal
    private String firstName;
    //    lastname will be avaialble from current authentication principal
    private String lastName;
    private String email;
    private String dob;
    private String age;
    private String gender;
    private String address;
    private String phoneNumber;
    private String maritalStatus;
    private String kin;
    private String kinNumber;
    private String kin1;
    private String kin1Number;
    private String nationalId;
    private int userId;
    private boolean isDocumentDataSubmitted;
    private boolean isUserImageSubmitted;
    private boolean isDigitalSignatureSubmitted;
    private Map loanAmountInfo;

    //    @Lob

}
