package com.appbackend.example.AppBackend.entities;

import com.appbackend.example.AppBackend.models.CreditScoreDtoDemo;
import com.fasterxml.jackson.annotation.JsonBackReference;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_score")
@Getter
@Setter
public class CreditScore implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private  User user;

    private String workPlaceDeartment;

    private String salaryScale;

//
    private String age;
//
//
    private String gender;
//
//
    private String daysInArrears;
//
//
    private String rescheduledHistory;


    private String priorityClient;

//
    private String nextOfKinType;

//    private String offerPerLevel;

    private Integer totalCreditScore;
    private Integer totalWeight;
    private Integer totalCreditScoreValue;
    private Float averageCreditScoreValue;
    private Float availableOffer;
    private Float totalExposure;



    private String workPlaceDepartment;
    private String occupation;
    private String amountInArrears;
    private String daysInArreas;
    private String security;
    private String loanHistoryLoansWithArrears;
    private String loanHistoryLoansWithOutArrears;
    private String blacklisted;



}
