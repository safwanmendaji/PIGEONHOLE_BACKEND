package com.appbackend.example.AppBackend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.appbackend.example.AppBackend.entities.KycCalculationDetails;
@Repository
public interface KycCalculationDetailsRepository extends JpaRepository<KycCalculationDetails, Integer> {


	List<KycCalculationDetails> findAllByWorkPlaceName(String workPlaceName);

}
