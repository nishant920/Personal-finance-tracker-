package com.personaltracker.finance.dtos;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private boolean verified;
}
