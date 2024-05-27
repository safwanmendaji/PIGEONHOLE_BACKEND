package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.models.InterestInfoMasterDto;
import org.springframework.http.ResponseEntity;

public interface InterestService {
    ResponseEntity addInterestCountMasterRecord(InterestInfoMasterDto dto);
    ResponseEntity updateInterestCountMasterRecord(InterestInfoMasterDto dto);

    ResponseEntity getInterestCountMasterInfo();

    ResponseEntity getInterestCountMasterInfoById(int id);
}
