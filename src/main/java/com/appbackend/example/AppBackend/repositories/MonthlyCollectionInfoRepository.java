package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.MonthlyCollectionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyCollectionInfoRepository extends JpaRepository<MonthlyCollectionInfo , Integer> {

    MonthlyCollectionInfo findFirstByDisbursementsHistoryIdOrderByIdDesc(Integer id);
}
