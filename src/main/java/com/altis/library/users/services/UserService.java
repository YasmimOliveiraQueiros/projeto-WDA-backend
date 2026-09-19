package com.altis.library.users.services;

import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;
import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;

import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse saveUser(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByCpf(userRequest.getCpf())) {
            throw new RuntimeException("CPF already registered");
        }

        User user = new User(
                userRequest.getName(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getPhone(),
                userRequest.getCpf(),
                userRequest.getBirthDate(),
                userRequest.getAddress()
        );

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

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ExpressionException("Usuário não encontrado"));

        return convertToResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ExpressionException("Usuário não encontrado"));

        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByCpfAndIdNot(userRequest.getCpf(), id)) {
            throw new RuntimeException("CPF already registered");
        }

        existingUser.setName(userRequest.getName());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setPassword(userRequest.getPassword());
        existingUser.setPhone(userRequest.getPhone());
        existingUser.setCpf(userRequest.getCpf());
        existingUser.setBirthDate(userRequest.getBirthDate());
        existingUser.setAddress(userRequest.getAddress());

        User updatedUser = userRepository.save(existingUser);

        return convertToResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ExpressionException("Usuário não encontrado"));

        userRepository.delete(user);
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
