package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.InterestCountMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<InterestCountMaster , Integer> {


    Optional<InterestCountMaster> findFirstByOrderById();
}
