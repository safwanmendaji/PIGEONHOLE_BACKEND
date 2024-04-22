package com.appbackend.example.AppBackend.repositories;

import com.appbackend.example.AppBackend.entities.KYC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;



public interface KYCRepository extends JpaRepository<KYC, Integer> {


//    Optional<KYC> findByuserId(Integer id);

    KYC findKYCById(Integer id);

	Optional<KYC> findByUserId(int id);



}
