package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.UserLoanEligibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLoanEligibilityRepository extends JpaRepository<UserLoanEligibility, Integer> {
    Optional<UserLoanEligibility> getByUserId(Integer id);
}
