package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementInterestCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


  @ManyToOne
  private DisbursementsHistory disbursementsHistory;


  @ManyToOne
  private InterestCountMaster interestCountMaster;


//  @ManyToOne
  private Integer userId;


  private LocalDate interestCalculationDate;


  private Long interestAmount;

  private Long endingBalance;

  private  Long beginningBalance;




}