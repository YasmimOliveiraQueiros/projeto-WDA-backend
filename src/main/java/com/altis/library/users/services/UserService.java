package com.altis.library.users.services;

import com.altis.library.exceptions.ConflictException;
import com.altis.library.exceptions.ResourceNotFoundException;
import com.altis.library.mappers.UserMapper;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.loans.models.enums.LoanStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;
import com.altis.library.users.models.dtos.PasswordChangeRequest;
import com.altis.library.users.models.dtos.UserProfileUpdateRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final LoanRepository loanRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            LoanRepository loanRepository) {
        this.userRepository = userRepository; // injeção de dependência por construtor, para poder usar dentro da classe
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.loanRepository = loanRepository;
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

    public Page<UserResponse> getAllUsers( // paginação
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

    public UserResponse getMyProfile(String email) {
        return userMapper.toResponse(findActiveCommonUser(email));
    }

    @Transactional
    public UserResponse updateMyProfile(
            String authenticatedEmail,
            UserProfileUpdateRequest request) {
        User user = findActiveCommonUser(authenticatedEmail);
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
            throw new ConflictException("O e-mail informado já está cadastrado.");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changeMyPassword(String authenticatedEmail, PasswordChangeRequest request) {
        User user = findActiveCommonUser(authenticatedEmail);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Senha atual inválida.");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Transactional
    public void closeMyAccount(String authenticatedEmail) {
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail);
        if (authenticatedUser == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }

        User user = userRepository.findByIdForUpdate(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (user.isAdmin() || !user.isActive()) {
            throw new AccessDeniedException("Acesso permitido somente a usuários comuns ativos.");
        }

        long pendingLoans = loanRepository.countByUserIdAndStatusIn(
                user.getId(),
                List.of(LoanStatus.PENDING, LoanStatus.OVERDUE)
        );
        if (pendingLoans > 0) {
            throw new ConflictException(
                    "Não é possível encerrar sua conta enquanto houver empréstimos pendentes. "
                            + "Regularize suas pendências antes de continuar."
            );
        }

        user.setActive(false);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    private User findActiveCommonUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || user.isAdmin() || !user.isActive()) {
            throw new AccessDeniedException("Acesso permitido somente a usuários comuns ativos.");
        }
        return user;
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

        existingUser.setName(mappedUser.getName()); // pega o nome novo e coloca no usuário que já existe
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
