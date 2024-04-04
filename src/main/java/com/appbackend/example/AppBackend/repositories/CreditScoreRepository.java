package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CreditScoreRepository extends JpaRepository<CreditScore,Integer> {

    CreditScore findCreditScoreById(Integer id);

    CreditScore findByUserId(Integer userId);


}
