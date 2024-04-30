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
    private User user;

    @ManyToOne
    private UserLoanEligibility userLoanEligibility;

    private Double availableBalance;

    private Double utilizeBalance;

    @OneToOne
    private DisbursementsHistory history;

    private LocalDateTime utilizeOn;


}
