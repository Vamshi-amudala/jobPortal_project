package com.example.dto;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String message;
    private String email;
}
