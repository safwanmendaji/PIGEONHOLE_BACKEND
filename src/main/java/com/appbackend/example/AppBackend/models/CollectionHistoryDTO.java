package com.appbackend.example.AppBackend.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectionHistoryDTO {

    private Integer id;
    private Integer disbursementsHistoryId;
    private String responseTransactionId;
    private String requestTransactionId;
    private double paymentAmount;
    private LocalDateTime paymentDate;
    private Integer userId;
    private String collectionRequest;
    private String status;
    private String response;
}
