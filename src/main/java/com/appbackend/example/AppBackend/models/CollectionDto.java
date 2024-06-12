package com.appbackend.example.AppBackend.models;

import lombok.Data;
@Data
public class CollectionDto {

    private String account;
    private Double amount;
    private Double mtnCharges;
    private Double mtnChargesAmount;
    private Double totalAmount;
    private Integer disbursementId;
}
