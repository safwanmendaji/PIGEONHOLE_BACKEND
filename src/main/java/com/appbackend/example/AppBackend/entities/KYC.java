package com.appbackend.example.AppBackend.entities;


import com.appbackend.example.AppBackend.enums.KycStatus;
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

    private String workId;

    private String gender;

    private String age;

    private String status;

    private String reason;



    private String documentData;


    private String  userImage;


    private String digitalSignature;


//
//    //    will be true on final submit
//    private boolean isSubmitted;


}
