package com.appbackend.example.AppBackend.models;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SuccessDto {
    private int code;
    private String status;
    private String message;
    private Object data;

    public SuccessDto(int code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
