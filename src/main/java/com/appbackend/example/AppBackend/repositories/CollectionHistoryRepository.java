package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.CollectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionHistoryRepository extends JpaRepository<CollectionHistory , Integer> {
    List<CollectionHistory> findByStatus(String name);

    List<CollectionHistory> findByDisbursementsHistoryIdOrderByIdDesc(Integer id);

    List<CollectionHistory> findByStatusIn(List<String> statuses);
}
