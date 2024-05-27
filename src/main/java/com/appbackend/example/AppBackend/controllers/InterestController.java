package com.appbackend.example.AppBackend.controllers;


import com.appbackend.example.AppBackend.models.InterestInfoMasterDto;
import com.appbackend.example.AppBackend.services.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(name = "interest")
public class InterestController {

    @Autowired
    private InterestService interestService;

    @PostMapping("/add/master")
    public ResponseEntity createInterestCountMaster(@RequestBody InterestInfoMasterDto dto){
        return interestService.addInterestCountMasterRecord(dto);
    }

    @GetMapping("/get/master")
    public ResponseEntity getInterestCountMasterInfo(){
        return interestService.getInterestCountMasterInfo();
    }

    @GetMapping("/get/master/detail/{id}")
    public ResponseEntity getInterestCountMasterInfoById(@PathVariable int id){
        return interestService.getInterestCountMasterInfoById(id);
    }


    @PutMapping("/update/master")
    public ResponseEntity updateInterestCountMaster(@RequestBody InterestInfoMasterDto dto){
        return interestService.addInterestCountMasterRecord(dto);
    }


}
