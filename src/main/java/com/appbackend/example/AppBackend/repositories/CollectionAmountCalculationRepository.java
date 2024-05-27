package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.CollectionAmountCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionAmountCalculationRepository extends JpaRepository<CollectionAmountCalculation , Integer> {
    Optional<CollectionAmountCalculation> findFirstByDisbursementsHistoryIdOrderByIdDesc(Integer id);
}
