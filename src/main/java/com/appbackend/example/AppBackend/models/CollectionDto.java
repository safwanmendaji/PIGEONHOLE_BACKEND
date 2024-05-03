package com.appbackend.example.AppBackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDto {

    private String type;
    private String account;
    private float amount;
    private UUID reference;
    private String narration;
}
