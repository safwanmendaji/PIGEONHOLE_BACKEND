package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DisbursementsRepository extends JpaRepository<DisbursementsHistory, Integer> {

    List<DisbursementsHistory> findByUserId(Integer userId);

    List<DisbursementsHistory> findByPaymentStatus(String pending);

    void deleteByUserId(int userId);

    boolean existsByUserIdAndCollectionCompletedTrue(int userId);
}
