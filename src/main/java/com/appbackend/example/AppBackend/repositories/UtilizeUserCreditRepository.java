package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.UtilizeUserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilizeUserCreditRepository extends JpaRepository<UtilizeUserCredit , Integer> {
    List<UtilizeUserCredit> findByUserIdOrderByIdDesc(Integer id);

    @Query("SELECT u FROM UtilizeUserCredit u WHERE u.user.id = :userId ORDER BY u.id DESC")
    UtilizeUserCredit findLatestByUserIdOrderByCreditScoreDesc(@Param("userId") Integer userId);

    UtilizeUserCredit findByHistoryId(Integer id);
}
