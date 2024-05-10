package com.appbackend.example.AppBackend.controllers;

import com.appbackend.example.AppBackend.models.ReferDto;
import com.appbackend.example.AppBackend.services.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/referral")
public class ReferralController {


    @Autowired
    private ReferralService referalService;

    @PostMapping("/newuser")
    public ResponseEntity<?> referralFriend(@RequestBody ReferDto referDto, Authentication authentication) {
        return referalService.referralFriend(referDto,authentication);

    }


    @PostMapping("/verify/code")
    public ResponseEntity<?> verifyReferralCode(@RequestBody ReferDto referDto) {
       return referalService.verifyReferralCode(referDto);
    }
}
