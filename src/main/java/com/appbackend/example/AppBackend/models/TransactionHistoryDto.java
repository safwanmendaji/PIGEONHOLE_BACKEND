package com.appbackend.example.AppBackend.models;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class TransactionHistoryDto {

    private Integer transactionHistoryId;
    private LocalDateTime dateTime;
    private String transactionId;
    private String status;
    private double amount;
    private String paymentFor;



}
