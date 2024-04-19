package com.appbackend.example.AppBackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="kyc_calculation_details")
public class KycCalculationDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name="work_place_name")
	private String workPlaceName;
	
	@Column(name="work_place_description")
	private String workPlaceDescription;
	
	@Column(name="risk_level")
	private String riskLevel;
	
	@Column(name="score")
	private int score;
	
	

}
