package com.appbackend.example.AppBackend.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DisbursementInterestDto {

    private Long disbursementAmount;
    private Double amountToPay;
    private Double minimumAmountToPay;
    private LocalDate lastInterestCountDate;
    private LocalDate nextInterestCountDate;
    private LocalDateTime lastPaymentDate;
    private Double lastPaidAmount;

}
