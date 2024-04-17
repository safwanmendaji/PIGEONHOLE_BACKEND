package com.appbackend.example.AppBackend.models.ForgotPasswordModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FpOtpVerify {
    String email;
    String otp;
}
