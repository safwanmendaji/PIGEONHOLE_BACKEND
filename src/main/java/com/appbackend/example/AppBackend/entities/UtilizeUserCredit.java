package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table
public class UtilizeUserCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "userLoanEligibility_id")
    private UserLoanEligibility userLoanEligibility;

    private Double availableBalance;

    private Double utilizeBalance;

    @ManyToOne
    @JoinColumn(name = "disbursementsHistory_id")
    private DisbursementsHistory history;

    private LocalDateTime utilizeOn;


}
