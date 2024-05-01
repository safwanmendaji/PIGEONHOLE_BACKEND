package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.UtilizeUserCredit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilizeUserCreditRepository extends JpaRepository<UtilizeUserCredit , Integer> {
    List<UtilizeUserCredit> findByUserIdOrderByIdDesc(Integer id);


    @Query(value = "SELECT * FROM utilize_user_credit WHERE user_id = :userId ORDER BY id DESC LIMIT 1", nativeQuery = true)
    UtilizeUserCredit findFirstByUserIdOrderByIdDesc(@Param("userId") Integer userId);


    UtilizeUserCredit findByHistoryId(Integer id);
}
