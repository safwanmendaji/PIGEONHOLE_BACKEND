package com.appbackend.example.AppBackend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DaysInArraysHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private DisbursementsHistory disbursementsHistory;

    private Integer userId;

    @OneToOne
    private MonthlyCollectionInfo monthlyCollectionInfo;

    private LocalDate enterArraysDate;
    private int totalDays;

}
