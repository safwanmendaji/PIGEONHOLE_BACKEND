package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisbursementsRepository extends JpaRepository<DisbursementsHistory, Integer> {
}
