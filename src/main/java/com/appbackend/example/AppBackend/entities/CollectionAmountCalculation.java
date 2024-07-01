package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CollectionAmountCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    private Integer userId;

    @ManyToOne
    private DisbursementsHistory disbursementsHistory;

    private Double remainingPayment;
    private Double payAmount;
    private Double totalPayAmount;
    private LocalDateTime lastTransactionDate;
    private String description;



}
