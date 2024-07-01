package com.appbackend.example.AppBackend.services;

import com.appbackend.example.AppBackend.enums.LoanReminder;

public interface LoanReminderService {


     String sentLoanReminderAfterLoanProcess(long days);

     String getReminderBeforeAndAfterDue(long days , boolean payAnyPayment);

     String getReminderForReschedule(long days );


}
