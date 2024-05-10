package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.models.ReferDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface ReferralService {
   

   

    ResponseEntity<?> referralFriend(ReferDto referDto, Authentication authentication);

    ResponseEntity<?> verifyReferralCode(ReferDto referDto);
}
