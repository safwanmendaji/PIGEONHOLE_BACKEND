package com.appbackend.example.AppBackend.models;

import lombok.Data;

@Data
public class ApprovalDeclineDto {
    private Integer id;
    private boolean approve;
    private String reason;

}
