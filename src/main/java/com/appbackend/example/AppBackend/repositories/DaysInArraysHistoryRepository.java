package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.DaysInArraysHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DaysInArraysHistoryRepository extends JpaRepository<DaysInArraysHistory , Integer> {
    DaysInArraysHistory findFirstByDisbursementsHistoryIdAndMonthlyCollectionInfoId(Integer id, Integer id1);
}
