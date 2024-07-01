package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.DisbursementInterestCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisbursementInterestCountRepository extends JpaRepository<DisbursementInterestCount , Integer> {
    DisbursementInterestCount findFirstByDisbursementsHistoryIdOrderByIdDesc(Integer id);
}
