package com.appbackend.example.AppBackend.models;

import jakarta.persistence.Lob;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
public class DisbursementHistoryDTO {


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
    private String disbursementsRequest;
    private Date startDate;
    private Date endDate;
    private String destination;
    private UUID referenceId;
    private String narration;
    private boolean approvedForTravel;

    private byte[] document;
    private String travelDeclineReason;
    private String reason;
    private String disbursementFailReason;
    private int approvedBy;
    private LocalDateTime approvedOn;
    private int updateBy;
    private LocalDateTime updateOn;
    private int createdBy;
    private LocalDateTime createdOn;
    private String email;
    private String mobile;

    private String username;
}
