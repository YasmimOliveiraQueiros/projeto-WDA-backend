package com.altis.library.users.services;

import com.altis.library.mappers.UserMapper;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.expression.ExpressionException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;

import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse saveUser(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByCpf(userRequest.getCpf())) {
            throw new RuntimeException("CPF already registered");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {

        List<User> users = userRepository.findByIsAdminFalse();

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findByIdAndIsAdminFalse(id)
                .orElseThrow(() -> new ExpressionException("Usuário não encontrado"));

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {

        User existingUser = userRepository.findByIdAndIsAdminFalse(id)
                .orElseThrow(() -> new ExpressionException("Usuário não encontrado"));

        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByCpfAndIdNot(userRequest.getCpf(), id)) {
            throw new RuntimeException("CPF already registered");
        }

        User mappedUser = userMapper.toEntity(userRequest);

        existingUser.setName(mappedUser.getName());
        existingUser.setEmail(mappedUser.getEmail());
        existingUser.setPhone(mappedUser.getPhone());
        existingUser.setCpf(mappedUser.getCpf());
        existingUser.setBirthDate(mappedUser.getBirthDate());
        existingUser.setAddress(mappedUser.getAddress());

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findByIdAndIsAdminFalse(id)
                .orElseThrow(() -> new ExpressionException("Usuário não encontrado"));

        userRepository.delete(user);
    }
}
