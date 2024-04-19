package com.appbackend.example.AppBackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.appbackend.example.AppBackend.entities.LoanEligibility;

@Repository
public interface LoanEligibilityRepository extends JpaRepository<LoanEligibility,Integer> {

}
