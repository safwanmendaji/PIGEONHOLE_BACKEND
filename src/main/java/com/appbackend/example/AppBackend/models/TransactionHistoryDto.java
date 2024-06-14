package com.appbackend.example.AppBackend.models;

import com.appbackend.example.AppBackend.enums.Disbursements;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class TransactionHistoryDto {

    private LocalDateTime dateTime;


    private String transactionId;

    private String status;


    private double amount;



}
