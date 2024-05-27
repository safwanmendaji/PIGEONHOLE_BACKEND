package com.appbackend.example.AppBackend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class DaysInArraysHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private DisbursementsHistory disbursementsHistory;

    @ManyToOne
    private User user;

    @ManyToOne
    private MonthlyCollectionInfo monthlyCollectionInfo;

}
