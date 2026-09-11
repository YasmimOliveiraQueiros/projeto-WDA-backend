package com.altis.library.users.services;

import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.stereotype.Service;
import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse saveUser(UserRequest userRequest) {

        User user = new User();

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        user.setPhone(userRequest.getPhone());
        user.setCpf(userRequest.getCpf());
        user.setBirthDate(userRequest.getBirthDate());
        user.setAddress(userRequest.getAddress());

        user.setAdmin(false);
        user.setActive(true);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            return convertToResponse(user);
        }

        return null;
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {

        User existingUser = userRepository.findById(id).orElse(null);

        if (existingUser != null) {

            existingUser.setName(userRequest.getName());
            existingUser.setEmail(userRequest.getEmail());
            existingUser.setPassword(userRequest.getPassword());
            existingUser.setPhone(userRequest.getPhone());
            existingUser.setCpf(userRequest.getCpf());
            existingUser.setBirthDate(userRequest.getBirthDate());
            existingUser.setAddress(userRequest.getAddress());

            existingUser.setUpdatedAt(LocalDateTime.now());

            User updatedUser = userRepository.save(existingUser);

            return convertToResponse(updatedUser);
        }

        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponse convertToResponse(User user) {

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
}
