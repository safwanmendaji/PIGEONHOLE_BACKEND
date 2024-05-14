package com.appbackend.example.AppBackend.entities;

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
    private String disbursementsRequest;
    private Date startDate;
    private Date endDate;
    private String destination;
    private UUID referenceId;
    private String narration;
    private boolean approvedForTravel;
    @Lob
    private String document;
    private String travelDeclineReason;
    private String reason;
    private String disbursementFailReason;
    private int approvedBy;
    private LocalDateTime approvedOn;
    private int updateBy;
    private LocalDateTime updateOn;
    private int createdBy;
    private LocalDateTime createdOn;

    private Boolean collectionCompleted;

//    @OneToMany(mappedBy = "history")
//    private List<UtilizeUserCredit> utilizeUserCredits;





}
