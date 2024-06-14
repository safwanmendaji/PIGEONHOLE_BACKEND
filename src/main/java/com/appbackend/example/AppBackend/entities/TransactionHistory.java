package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="transaction_History_Id")
    private int transactionHistoryId;

    @ManyToOne
    @JoinColumn(name = "disbursements_history_id")
    private DisbursementsHistory disbursementsHistory;

    @ManyToOne
    @JoinColumn(name = "collection_history_id")
    private CollectionHistory collectionHistory;


    private Integer userId;

    private LocalDateTime localDateTime;
}
