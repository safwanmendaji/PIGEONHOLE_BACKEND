package com.appbackend.example.AppBackend.controllers;


import com.appbackend.example.AppBackend.models.InterestInfoMasterDto;
import com.appbackend.example.AppBackend.services.InterestService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(name = "interest")
public class InterestController {

    Logger logger = LoggerFactory.getLogger(InterestController.class);

    @Autowired
    private InterestService interestService;

    @PostMapping("/add/master")
    public ResponseEntity createInterestCountMaster(@RequestBody InterestInfoMasterDto dto){
        logger.info("Inside CreateInterestCountMaster Method in Interest Controller");
        return interestService.addInterestCountMasterRecord(dto);
    }

    @GetMapping("/get/master")
    public ResponseEntity getInterestCountMasterInfo(){
        logger.info("Inside GetInterestCountMasterInfo Method in Interest Controller");
        return interestService.getInterestCountMasterInfo();
    }

    @GetMapping("/get/master/detail/{id}")
    public ResponseEntity getInterestCountMasterInfoById(@PathVariable int id){
        logger.info("Inside GetInterestCountMAsterInfoById Method in Controller");
        return interestService.getInterestCountMasterInfoById(id);
    }


    @PutMapping("/update/master")
    public ResponseEntity updateInterestCountMaster(@RequestBody InterestInfoMasterDto dto){
        logger.info("Inside UpdateInterestCountMaster Method in Controller");
        return interestService.addInterestCountMasterRecord(dto);
    }


}
