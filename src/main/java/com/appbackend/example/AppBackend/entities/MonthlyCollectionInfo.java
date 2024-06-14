package com.appbackend.example.AppBackend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MonthlyCollectionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate monthStartDate;
    private LocalDate monthEndDate;
    private Double minimumAmount;
    private Double totalPayAmountInMonth;
    private boolean payMinimumAmount;
    private boolean isLastMonth;
    private Boolean isRescheduled;
    private LocalDate rescheduleDate;

    @ManyToOne
    private DisbursementsHistory disbursementsHistory;



}
