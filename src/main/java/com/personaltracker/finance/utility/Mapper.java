package com.personaltracker.finance.utility;

import com.personaltracker.finance.dtos.UserDto;
import com.personaltracker.finance.dtos.UserResponseDto;
import com.personaltracker.finance.models.User;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public UserResponseDto mapUserToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setVerified(user.isVerified());
        return dto;
    }

    public User mapUserDetailsToUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        return user;
    }
}
