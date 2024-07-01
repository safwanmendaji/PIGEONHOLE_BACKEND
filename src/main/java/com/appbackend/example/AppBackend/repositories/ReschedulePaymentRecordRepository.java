package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.ReschedulePaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReschedulePaymentRecordRepository extends JpaRepository<ReschedulePaymentRecord , Integer> {
    List<ReschedulePaymentRecord> findByDisbursementsHistoryId(Integer id);
}
