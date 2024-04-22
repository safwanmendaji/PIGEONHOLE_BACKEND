package com.appbackend.example.AppBackend.entities;

import com.appbackend.example.AppBackend.enums.DisbursementsType;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    private Integer userId;

    private DisbursementsType disbursementsType;
    private float amount;
    private String disbursementsTransactionId;
    private String disbursementsResponse;
    private DisbursementsStatus paymentStatus;
    private int approvedBy;
    private LocalDateTime approvedOn;
    private int updateBy;
    private LocalDateTime updateOn;
    private int createdBy;
    private LocalDateTime createdOn;


}
