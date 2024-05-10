package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.common.AppCommon;

import com.appbackend.example.AppBackend.entities.ReferralInfo;
import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.models.ReferDto;
import com.appbackend.example.AppBackend.repositories.ReferralRepository;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import com.appbackend.example.AppBackend.services.OtpService;
import com.appbackend.example.AppBackend.services.ReferralService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReferralServiceImpl implements ReferralService {

    @Autowired
    private OtpService emailOtpService;

    @Autowired
    private AppCommon appCommon;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReferralRepository referralRepository;
    @Override
    public ResponseEntity<?> referralFriend(ReferDto referDto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();
        Optional<User> optionalUser = userRepository.findById(userId);


        String email = referDto.getEmail();
        String mobile = referDto.getMobile();

        try {
            Optional<ReferralInfo> existingReferralByEmail = referralRepository.findByReferalEmail(email);
            Optional<ReferralInfo> existingReferralByPhone = referralRepository.findByReferalMobile(mobile);

            if (existingReferralByEmail.isPresent()) {
                int referringUserIdByEmail = existingReferralByEmail.get().getUserId();
                String referringUserFirstNameByEmail = optionalUser.get().getFirstName();
                return ResponseEntity.badRequest().body("The email " + email + " is already referred by user " + referringUserFirstNameByEmail);
            }

            if (existingReferralByPhone.isPresent()) {
                int referringUserIdByPhone = existingReferralByPhone.get().getUserId();
                String referringUserFirstNameByPhone =  optionalUser.get().getFirstName();
                return ResponseEntity.badRequest().body("The mobile number " + mobile + " is already referred by user " + referringUserFirstNameByPhone);
            }



            String combinedData = email + "|" + mobile;
            String encryptedData = appCommon.encrypt(combinedData);
            emailOtpService.sendReferCodeViaEmail(email, encryptedData);

            ReferralInfo referralInfo = new ReferralInfo();
            referralInfo.setReferalMobile(mobile);
            referralInfo.setReferalEmail(email);
            referralInfo.setUserId(userId);
            referralInfo.setReferralString(encryptedData);
            referralInfo.setDateTime(LocalDateTime.now());

            referralRepository.save(referralInfo);

            return ResponseEntity.ok("Referral code sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL SERVER ERROR");
        }
    }



    @Override
    public ResponseEntity<?> verifyReferralCode(ReferDto referDto) {
        try {
            String decryptedReferCode = appCommon.decrypt(referDto.getReferenceCode());
            String[] parts = decryptedReferCode.split("\\|");
            if (parts.length == 2) {
                String decryptedEmail = parts[0];
                String decryptedMobile = parts[1];
                if (decryptedEmail.equals(referDto.getEmail()) && decryptedMobile.equals(referDto.getMobile())) {
                    return ResponseEntity.ok("Reference code verified successfully.");
                } else {
                    return ResponseEntity.badRequest().body("Reference code is invalid.");
                }
            } else {
                return ResponseEntity.badRequest().body("Invalid format of reference code.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying reference code.");
        }
    }
}
