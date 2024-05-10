package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.ReferralInfo;
import com.appbackend.example.AppBackend.entities.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<ReferralInfo,Integer> {

    Optional<ReferralInfo> findByReferalEmail(String email);

    Optional<ReferralInfo> findByReferalMobile(String mobile);
}
