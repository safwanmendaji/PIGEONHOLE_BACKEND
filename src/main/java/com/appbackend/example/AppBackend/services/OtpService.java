package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.entities.User;
import com.appbackend.example.AppBackend.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;


@Service
public class OtpService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.password}")
    private String smsPassword;

    @Value("${sms.username}")
    private String smsUserName;

    private AuthenticationEntryPoint authenticationEntryPoint;


//    private User user;


    public String saveOtp(User user) {
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(Instant.now());
        userRepository.save(user);
        return otp;
    }

    public void sendVerificationOtp(User user , String username){
        String otp =  saveOtp(user);
        System.out.println("==>>> " + user.getUsername());
        if(username.contains("@")) {
            sendVerificationOtpEmail(user, otp);
        }else{
            sendVerificationOtpPhone(user , otp);
        }
    }

    public void sendVerificationOtpPhone(User user , String otp){
        try {
            String subject = "Verification";
            String body = "Your Verification Code is " + otp;
            sendSms(user.getPhoneNumber(), subject, body);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendVerificationOtpEmail(User user , String otp) {
        String subject = "Email Verification";
        String body = "your verification otp is " + otp;
        sendEmail(user.getEmail(), subject, body);
    }


    public String verifyOtp(String reqUserOtp, User savedUser) throws Exception {
        if (Duration.between(savedUser.getOtpGeneratedTime(), Instant.now()).getSeconds() < 120) {
            if (reqUserOtp.equals(savedUser.getOtp())) {
                savedUser.setLoginTimeStamp(Instant.now());
                return "you are logged in successfully";
            } else {
                throw new Exception("otp is Invalid ");
            }
        } else {
            throw new Exception("your one-time password (otp) has expired");
        }

//        }
//        else {
//            throw new RuntimeException("The user email is not found");
//        }

    }

    public String verifyFpwOtp(String reqUserOtp, User savedUser) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        UserDetails userDetails = userDetailsService.loadUserByUsername(otpRequest.getUserEmail());


//        System.out.println(reqUserEmail.equals(authentication.getName()));
//        System.out.println("reqUserEmail"+reqUserEmail);
//        System.out.println("authuser"+authentication.getName());

//        if(reqUserEmail.equals(savedUser.getUsername())){

//            User savedUser=userRepository.findByEmail(reqUserEmail).get();

        if (Duration.between(savedUser.getOtpGeneratedTime(), Instant.now()).getSeconds() < 120) {
            if (reqUserOtp.equals(savedUser.getOtp())) {
//                savedUser.setLoginTimeStamp(Instant.now());
                return "now you can change your password ";
            } else {
                throw new RuntimeException("otp is Invalid ");
            }
        } else {
            throw new RuntimeException("your one-time password (otp) has expired");
        }

//        }
//        else {
//            throw new RuntimeException("The user email is not found");
//        }

    }

    public String generateOtp() {
        SecureRandom secureRandom = new SecureRandom();
        int otpValue = 1000 + secureRandom.nextInt(8999);
        return String.valueOf(otpValue);
    }


    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom("190320107124.ce.zishan.shaikh@gmail.com");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            e.getMessage();
            throw new RuntimeException(e);

        }
    }

    public void sendSms(String mobileNumber, String subject, String body) throws Exception {
        // The details of the message we want to send
        String myData = String.format("{to: \"%s\", encoding: \"UNICODE\", body: \"%s\"}", mobileNumber, body);

        // Build the request based on the supplied settings
        URL url = new URL(smsUrl);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setDoOutput(true);

        // Supply the credentials
        String authStr = smsUserName + ":" + smsPassword;
        String authEncoded = Base64.getEncoder().encodeToString(authStr.getBytes());
        request.setRequestProperty("Authorization", "Basic " + authEncoded);

        // We want to use HTTP POST
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type", "application/json");

        // Write the data to the request
        try (OutputStreamWriter out = new OutputStreamWriter(request.getOutputStream())) {
            out.write(myData);
        }

        // Try ... catch to handle errors nicely
        try {
            // Make the call to the API
            InputStream response = request.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(response));
            String replyText;
            while ((replyText = in.readLine()) != null) {
                System.out.println(replyText);
            }
            in.close();
        } catch (IOException ex) {
            System.out.println("An error occurred:" + ex.getMessage());
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
            // Print the detail that comes with the error
            String replyText;
            while ((replyText = in.readLine()) != null) {
                System.out.println(replyText);
            }
            in.close();
        } finally {
            request.disconnect();
        }
    }


}
