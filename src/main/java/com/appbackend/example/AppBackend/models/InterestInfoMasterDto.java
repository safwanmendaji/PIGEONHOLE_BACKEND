package com.appbackend.example.AppBackend.models;

import com.appbackend.example.AppBackend.enums.InterestFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestInfoMasterDto {

    private Integer id;
    private Integer interestRate;
    private InterestFrequency interestFrequency;
    private Integer interestFrequencyNo;
}
