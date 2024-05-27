package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.entities.InterestCountMaster;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.ErrorDto;
import com.appbackend.example.AppBackend.models.InterestInfoMasterDto;
import com.appbackend.example.AppBackend.models.SuccessDto;
import com.appbackend.example.AppBackend.repositories.InterestRepository;
import com.appbackend.example.AppBackend.services.InterestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InterestServiceImpl implements InterestService {

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity addInterestCountMasterRecord(InterestInfoMasterDto dto) {
        try{
           InterestCountMaster interestCountMaster =     objectMapper.convertValue(dto , InterestCountMaster.class);
           interestCountMaster = interestRepository.save(interestCountMaster);

            SuccessDto successResponse = SuccessDto.builder()
                    .code(HttpStatus.OK.value())
                    .status("Success")
                    .message("Record added successfully.")
                    .data(interestCountMaster)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(successResponse);
        }catch (Exception e){
            e.printStackTrace();
            ErrorDto errorResponse = ErrorDto.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("Error")
                    .message("Some thing when wrong.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity updateInterestCountMasterRecord(InterestInfoMasterDto dto) {
        try{
            Optional<InterestCountMaster> interestCountMasterOptional = interestRepository.findFirstByOrderById();
            if(interestCountMasterOptional.isPresent()) {
                InterestCountMaster interestCountMaster = objectMapper.convertValue(dto, InterestCountMaster.class);
                interestCountMaster = interestRepository.save(interestCountMaster);

                SuccessDto successResponse = SuccessDto.builder()
                        .code(HttpStatus.OK.value())
                        .status("Success")
                        .message("Record added successfully.")
                        .data(interestCountMaster)
                        .build();
                return ResponseEntity.status(HttpStatus.OK).body(successResponse);
            }else {
                ErrorDto errorResponse = ErrorDto.builder()
                        .code(HttpStatus.NOT_FOUND.value())
                        .status("Error")
                        .message("No record found.")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        }catch (Exception e){
            e.printStackTrace();
            ErrorDto errorResponse = ErrorDto.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("Error")
                    .message("Some thing when wrong.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity getInterestCountMasterInfo() {
        try{
            Optional<InterestCountMaster> interestCountMasterOptional = interestRepository.findFirstByOrderById();
           if(interestCountMasterOptional.isPresent()){

               InterestInfoMasterDto interestInfoMasterDto = objectMapper.convertValue(interestCountMasterOptional.get() , InterestInfoMasterDto.class);

               SuccessDto successResponse = SuccessDto.builder()
                       .code(HttpStatus.OK.value())
                       .status("Success")
                       .message("Get record successfully.")
                       .data(interestInfoMasterDto)
                       .build();
               return ResponseEntity.status(HttpStatus.OK).body(successResponse);
           }else{
               ErrorDto errorResponse = ErrorDto.builder()
                       .code(HttpStatus.NOT_FOUND.value())
                       .status("Error")
                       .message("No record found.")
                       .build();
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
           }


        }catch (Exception e){
            e.printStackTrace();
            ErrorDto errorResponse = ErrorDto.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("Error")
                    .message("Some thing when wrong.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity getInterestCountMasterInfoById(int id) {
        try{
            Optional<InterestCountMaster> interestCountMasterOptional = interestRepository.findById(id);
            if(interestCountMasterOptional.isPresent()){
                InterestInfoMasterDto interestInfoMasterDto = objectMapper.convertValue(interestCountMasterOptional.get() , InterestInfoMasterDto.class);

                SuccessDto successResponse = SuccessDto.builder()
                        .code(HttpStatus.OK.value())
                        .status("Success")
                        .message("Get record successfully.")
                        .data(interestInfoMasterDto)
                        .build();
                return ResponseEntity.status(HttpStatus.OK).body(successResponse);
            }else{
                ErrorDto errorResponse = ErrorDto.builder()
                        .code(HttpStatus.NOT_FOUND.value())
                        .status("Error")
                        .message("No record found.")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }


        }catch (Exception e){
            e.printStackTrace();
            ErrorDto errorResponse = ErrorDto.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status("Error")
                    .message("Some thing when wrong.")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


}
