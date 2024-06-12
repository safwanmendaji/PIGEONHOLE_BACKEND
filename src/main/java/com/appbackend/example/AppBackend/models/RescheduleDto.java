package com.appbackend.example.AppBackend.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RescheduleDto {
    private long disbursementId;
    private LocalDate rescheduledDate;


}
