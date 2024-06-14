package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data

public class ReschedulePaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int userId;
    @ManyToOne
    private MonthlyCollectionInfo monthlyCollectionInfo;
    @ManyToOne
    private DisbursementsHistory disbursementsHistory;
    private LocalDate rescheduleDate;


}
