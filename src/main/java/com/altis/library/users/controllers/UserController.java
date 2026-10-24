package com.altis.library.users.controllers;

import com.altis.library.users.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.altis.library.users.models.dtos.UserRequest;
import com.altis.library.users.models.dtos.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.altis.library.users.models.dtos.PasswordChangeRequest;
import com.altis.library.users.models.dtos.UserProfileUpdateRequest;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth") // funciona como chave de acesso com as permissões de quem carrega
public class UserController {

    private final UserService userService; // declara uma variável chamada userService que será usada para acessar os métodos da classe UserService.

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(userService.getMyProfile(email));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(email, request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changeMyPassword(email, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> closeMyAccount(@AuthenticationPrincipal String email) {
        userService.closeMyAccount(email);
        return ResponseEntity.noContent().build();
    }

    // create
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.saveUser(userRequest));
    }

    // get all
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String search, // pega as informações dos parâmetros
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        return ResponseEntity.ok(userService.getAllUsers(
                search,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    // get by id
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // update
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {

        return userService.updateUser(id, userRequest);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                userService.updateStatus(id, active)
        );
    }
}