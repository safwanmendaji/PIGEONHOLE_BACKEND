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
public class CollectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private DisbursementsHistory disbursementsHistory;

    private String responseTransactionId;

    private String requestTransactionId;

    private double paymentAmount;

    private LocalDateTime paymentDate;

    @ManyToOne
    private User user;
    private String collectionRequest;
    private String status;
    private String response;


}
