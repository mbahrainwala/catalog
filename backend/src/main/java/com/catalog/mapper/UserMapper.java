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
        
        UserDto dto = new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getEnabled(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
        
        // Map additional profile fields
        dto.setAlternateEmail(user.getAlternateEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAlternatePhoneNumber(user.getAlternatePhoneNumber());
        
        // Address 1
        dto.setAddress1Line1(user.getAddress1Line1());
        dto.setAddress1Line2(user.getAddress1Line2());
        dto.setAddress1City(user.getAddress1City());
        dto.setAddress1State(user.getAddress1State());
        dto.setAddress1PostalCode(user.getAddress1PostalCode());
        dto.setAddress1Country(user.getAddress1Country());
        
        // Address 2
        dto.setAddress2Line1(user.getAddress2Line1());
        dto.setAddress2Line2(user.getAddress2Line2());
        dto.setAddress2City(user.getAddress2City());
        dto.setAddress2State(user.getAddress2State());
        dto.setAddress2PostalCode(user.getAddress2PostalCode());
        dto.setAddress2Country(user.getAddress2Country());
        
        // Documents and images
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setIdDocument1Url(user.getIdDocument1Url());
        dto.setIdDocument1Filename(user.getIdDocument1Filename());
        dto.setIdDocument2Url(user.getIdDocument2Url());
        dto.setIdDocument2Filename(user.getIdDocument2Filename());
        
        return dto;
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
        
        // Map additional profile fields
        user.setAlternateEmail(userDto.getAlternateEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAlternatePhoneNumber(userDto.getAlternatePhoneNumber());
        
        // Address 1
        user.setAddress1Line1(userDto.getAddress1Line1());
        user.setAddress1Line2(userDto.getAddress1Line2());
        user.setAddress1City(userDto.getAddress1City());
        user.setAddress1State(userDto.getAddress1State());
        user.setAddress1PostalCode(userDto.getAddress1PostalCode());
        user.setAddress1Country(userDto.getAddress1Country());
        
        // Address 2
        user.setAddress2Line1(userDto.getAddress2Line1());
        user.setAddress2Line2(userDto.getAddress2Line2());
        user.setAddress2City(userDto.getAddress2City());
        user.setAddress2State(userDto.getAddress2State());
        user.setAddress2PostalCode(userDto.getAddress2PostalCode());
        user.setAddress2Country(userDto.getAddress2Country());
        
        // Documents and images
        user.setProfilePictureUrl(userDto.getProfilePictureUrl());
        user.setIdDocument1Url(userDto.getIdDocument1Url());
        user.setIdDocument1Filename(userDto.getIdDocument1Filename());
        user.setIdDocument2Url(userDto.getIdDocument2Url());
        user.setIdDocument2Filename(userDto.getIdDocument2Filename());
        
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