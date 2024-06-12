package com.appbackend.example.AppBackend.common;

import com.appbackend.example.AppBackend.entities.CollectionHistory;
import com.appbackend.example.AppBackend.entities.DisbursementsHistory;
import com.appbackend.example.AppBackend.enums.DisbursementsStatus;
import com.appbackend.example.AppBackend.repositories.CollectionHistoryRepository;
import com.appbackend.example.AppBackend.repositories.DisbursementInterestCountRepository;
import com.appbackend.example.AppBackend.repositories.DisbursementsRepository;
import com.appbackend.example.AppBackend.services.CollectionService;
import com.appbackend.example.AppBackend.services.PaymentService;
import com.appbackend.example.AppBackend.services.impl.CollectionServiceImpl;
import com.appbackend.example.AppBackend.services.impl.PaymentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Schedulers {

    Logger logger = LoggerFactory.getLogger(Schedulers.class);
    @Autowired
    private DisbursementsRepository disbursementsRepository;

    @Autowired
    private CollectionHistoryRepository collectionHistoryRepository;

    @Autowired
    private DisbursementInterestCountRepository disbursementInterestCountRepository;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CollectionService collectionService;

    @Scheduled(fixedRate = 150000)
    public  void updateDisbursementStatusAndUtilization(){
        System.out.println("Run Schedule:::");
        List<DisbursementsHistory> disbursementsHistoryList = disbursementsRepository.findByPaymentStatus(DisbursementsStatus.PENDING.name());
        System.out.println("Size Of Pending Status :: " + disbursementsHistoryList.size());
        PaymentServiceImpl paymentServiceImpl = new PaymentServiceImpl();
        disbursementsHistoryList.forEach(paymentServiceImpl::checkDisbursementStatusAndUpdate);

        List<CollectionHistory> collectionHistoryList = collectionHistoryRepository.findByStatus(DisbursementsStatus.PENDING.name());
        logger.info("Size Of Pending Status for collection :: " + collectionHistoryList.size());
        collectionHistoryList.forEach(collectionService::checkCollectionStatusAndUpdate);
    }


    @Scheduled(cron = "0 0 0 * * *")
    public void dailyJobScheduler(){
        // first need to check the end of month for all loan
//        List<DisbursementsHistory> disbursementsHistoryList = disbursementsRepository
//        DisbursementInterestCount disbursementInterestCount = disbursementInterestCountRepository.findFistByDisbursementsHistoryIdOrderByIdDesc();
//        long interestCountDays = ChronoUnit.DAYS.between(disbursementInterestCount.getInterestCalculationDate(), LocalDate.now());


        //check the all user payment monthly

        //
    }

}
