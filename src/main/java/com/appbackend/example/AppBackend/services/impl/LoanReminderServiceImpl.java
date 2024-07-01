package com.appbackend.example.AppBackend.services.impl;

import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.entities.LoanReminderMessages;
import com.appbackend.example.AppBackend.entities.MonthlyCollectionInfo;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.enums.LoanReminder;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.repositories.LoanReminderRepository;
import com.appbackend.example.AppBackend.repositories.MonthlyCollectionInfoRepository;
import com.appbackend.example.AppBackend.services.LoanReminderService;
import com.appbackend.example.AppBackend.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class LoanReminderServiceImpl implements LoanReminderService {

    @Autowired
    private LoanReminderRepository loanReminderRepository;

    @Autowired
    private DisbursementsRepository disbursementsRepository;
    @Autowired
    private MonthlyCollectionInfoRepository monthlyCollectionInfoRepository;

    @Autowired
    private OtpService otpService;


    public void sendReminderForLoan(){

    }

    @Override
    public String sentLoanReminderAfterLoanProcess(long daysUntilDue) {

        LoanReminder loanReminder = getLoanReminder(daysUntilDue);
        if(loanReminder != null){
            LoanReminderMessages reminderMessages = loanReminderRepository.findByLoanReminder(loanReminder.name());
             if (reminderMessages != null) {
               return reminderMessages.getMessage();
            }
        }

        return null;

    }

    private static LoanReminder getLoanReminder(long daysUntilDue) {
        if (daysUntilDue == 1) {
            return LoanReminder.ONE_DAY_AFTER_LOAN;
        } else if (daysUntilDue == 15) {
            return LoanReminder.TWO_WEEKS_AFTER_LOAN;
        }else {
            return null;
        }
    }


    @Override
    public String getReminderBeforeAndAfterDue(long daysUntilDue , boolean payAnyAmount){
        LoanReminder loanReminder = getReminderForDue(daysUntilDue , payAnyAmount);
        if(loanReminder != null){
            LoanReminderMessages reminderMessages = loanReminderRepository.findByLoanReminder(loanReminder.name());
            if (reminderMessages != null) {
                return reminderMessages.getMessage();
            }
        }
         return  null;
    }

    @Override
    public String getReminderForReschedule(long days) {
        LoanReminder loanReminder = getReminderForRes(days);
        if(loanReminder != null){
            LoanReminderMessages reminderMessages = loanReminderRepository.findByLoanReminder(loanReminder.name());
            if (reminderMessages != null) {
                return reminderMessages.getMessage();
            }
        }
        return  null;
    }

    private static LoanReminder getReminderForRes(long daysUntilDue) {
        if (daysUntilDue == 1) {
            return LoanReminder.ONE_DAY_AFTER_RESCHEDULED;
        } else if (daysUntilDue == 15) {
            return LoanReminder.ONE_DAY_AFTER_RESCHEDULED;
        }else if (daysUntilDue == 0) {
            return LoanReminder.RESCHEDULED_DUE_DATE;
        }else {
            return null;
        }
    }

    private static LoanReminder getReminderForDue(long daysUntilDue , boolean payAnyAmount) {


        // No payment yet receive
        if(payAnyAmount) {
            if (daysUntilDue == 1) {
                return LoanReminder.DUE_DATE;
            } else if (daysUntilDue == 3) {
                return LoanReminder.THREE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 5) {
                return LoanReminder.FIVE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 7) {
                return LoanReminder.SEVEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 9) {
                return LoanReminder.NINE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 11) {
                return LoanReminder.ELEVEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 13) {
                return LoanReminder.THIRTEEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 15) {
                return LoanReminder.FIFTEEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 17) {
                return LoanReminder.SEVENTEEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 19) {
                return LoanReminder.NINETEEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 21) {
                return LoanReminder.TWENTY_ONE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 23) {
                return LoanReminder.TWENTY_THREE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 25) {
                return LoanReminder.TWENTY_FIVE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 27) {
                return LoanReminder.TWENTY_SEVEN_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 29) {
                return LoanReminder.TWENTY_NINE_DAYS_AFTER_DUE;
            } else if (daysUntilDue == 30) {
                return LoanReminder.THIRTY_DAYS_AFTER_DUE;
            }
        }else{
            if (daysUntilDue == -7) {
                return LoanReminder.ONE_WEEK_BEFORE;
            } else if (daysUntilDue == 0) {
                return LoanReminder.DUE_DATE;
            }else{
                return null;
            }
        }

        return  null;
    }


//    private String getMessageForReminder(LoanReminder reminder) {
//        if (reminder == null) {
//            return "";
//        }
//
//        // Fetch message from database based on the reminder
//        LoanReminderMessages reminderMessages = reminderMessagesRepository.findByLoanReminder(reminder);
//        if (reminderMessages != null) {
//            return reminderMessages.getMessage();
//        }
//
//        return "";
//    }

}
