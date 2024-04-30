package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="user_loan_eligibility")
public class UserLoanEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @OneToOne
    private LoanEligibility eligibility;

    @Column(name="eligibility_amount")
    private Long eligibilityAmount;

    @Column(name="old_eligibility_amount")
    private Long oldEligibilityAmount;


    @OneToOne
    private User user;

}
