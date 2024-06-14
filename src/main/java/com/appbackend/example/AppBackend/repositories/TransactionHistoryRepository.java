package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.TransactionHistory;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Integer> {

    List<TransactionHistory> findByUserIdOrderByTransactionHistoryIdDesc(Integer userId);
}
