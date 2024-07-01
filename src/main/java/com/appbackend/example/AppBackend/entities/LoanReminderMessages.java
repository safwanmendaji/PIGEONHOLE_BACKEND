package com.appbackend.example.AppBackend.entities;

import com.appbackend.example.AppBackend.enums.LoanReminder;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class LoanReminderMessages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    private LoanReminder loanReminder;
    @Column(length = 1000)
    private String message;
    @Column(length = 500)
    private String description;
}
