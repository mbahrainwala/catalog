package com.catalog.mapper;

import com.catalog.dto.UserDto;
import com.catalog.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getEnabled(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        
        User user = new User();
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(User.Role.valueOf(userDto.getRole()));
        user.setEnabled(userDto.getEnabled());
        user.setCreatedAt(userDto.getCreatedAt());
        user.setUpdatedAt(userDto.getUpdatedAt());
        
        return user;
    }
    
    public List<UserDto> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<User> toEntityList(List<UserDto> userDtos) {
        if (userDtos == null) {
            return null;
        }
        
        return userDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}