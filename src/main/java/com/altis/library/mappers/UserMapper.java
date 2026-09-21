package com.altis.library.mappers;

import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;
import com.altis.library.users.models.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setCpf(user.getCpf());
        response.setBirthDate(user.getBirthDate());
        response.setAddress(user.getAddress());
        response.setAdmin(user.isAdmin());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    public User toEntity(UserRequest userRequest) {
        return new User(
                userRequest.getName(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getPhone(),
                userRequest.getCpf(),
                userRequest.getBirthDate(),
                userRequest.getAddress()
        );
    }
}
