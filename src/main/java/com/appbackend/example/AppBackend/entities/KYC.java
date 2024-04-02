package com.appbackend.example.AppBackend.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "KYC",indexes = {
        @Index(name="idx_userid",columnList = "id")
}
)

public class KYC implements Serializable {

    @Id
    private Integer id;

    @OneToOne
    private User user;


    private String dob;

//    private String phoneNumber;

    private String address;
    //
    private String maritalStatus;

    private String kin;

    private String kinNumber;

    private String kin1;

    private String kin1Number;

    private String nationalId;

    private String gender;

    private String age;


    @Lob
    private byte[] documentData;

    @Lob
    private byte[] userImage;

    @Lob
    private byte[] digitalSignature;
//
//    //    will be true on final submit
//    private boolean isSubmitted;


}
