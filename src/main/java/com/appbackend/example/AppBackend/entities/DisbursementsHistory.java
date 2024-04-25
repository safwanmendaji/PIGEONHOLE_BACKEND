package com.appbackend.example.AppBackend.entities;

import com.appbackend.example.AppBackend.enums.DisbursementsType;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

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

    private String disbursementsType;
    private float amount;
    private String disbursementsTransactionId;
    private String disbursementsResponse;
    private String paymentStatus;
    private String studentCode;
    private String studentName;
    private String schoolName;
    private String studentClass;
    private Double outstandingFees;
    private String teamLeadName;
    private String teamLeadContactNumber;
    private Date startDate;
    private Date endDate;
    private String destination;
    private UUID referenceId;
    private String narration;
    @Lob
    private byte[] document;
    private String reason;
    private int approvedBy;
    private LocalDateTime approvedOn;
    private int updateBy;
    private LocalDateTime updateOn;
    private int createdBy;
    private LocalDateTime createdOn;





}
