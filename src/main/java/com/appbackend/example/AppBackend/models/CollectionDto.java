package com.appbackend.example.AppBackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDto {

    private String account;
    private Double amount;
    private Double mtnCharges;
    private Double mtnChargesAmount;
    private Double totalAmount;
    private Integer disbursementId;
}
