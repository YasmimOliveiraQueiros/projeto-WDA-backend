package com.altis.library.users.services;

import com.altis.library.exceptions.ConflictException;
import com.altis.library.exceptions.ResourceNotFoundException;
import com.altis.library.mappers.UserMapper;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.altis.library.users.repositories.UserSpecification;

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
            throw new ConflictException("O e-mail informado já está cadastrado.");
        }

        if (userRepository.existsByCpf(userRequest.getCpf())) {
            throw new ConflictException("O CPF informado já está cadastrado.");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public Page<UserResponse> getAllUsers(
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                resolveSort(sortBy, sortDirection)
        );

        Specification<User> userSpecification =
                UserSpecification.searchSpecification(search);

        return userRepository.findAll(userSpecification, pageable)
                .map(userMapper::toResponse);
    }

    private Sort resolveSort(String sortBy, String sortDirection) {
        String property = switch (sortBy == null ? "" : sortBy.trim()) {
            case "id", "name", "email", "birthDate", "createdAt", "active" ->
                    sortBy.trim();
            default -> throw new IllegalArgumentException(
                    "Campo de ordenação inválido."
            );
        };

        Sort.Direction direction;
        try {
            direction = Sort.Direction.valueOf(sortDirection.trim().toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "A direção da ordenação deve ser ASC ou DESC."
            );
        }

        return Sort.by(direction, property);
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findByIdAndIsAdminFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado."));

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {

        User existingUser = userRepository.findByIdAndIsAdminFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado."));

        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new ConflictException("O e-mail informado já está cadastrado.");
        }

        if (userRepository.existsByCpfAndIdNot(userRequest.getCpf(), id)) {
            throw new ConflictException("O CPF informado já está cadastrado.");
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

    public UserResponse updateStatus(Long id, boolean active) {

        User user = userRepository.findByIdAndIsAdminFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado."));

        user.setActive(active);

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }
}
