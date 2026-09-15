package com.altis.library.loans.controllers;

import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.services.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll() {
        return ResponseEntity.ok(loanService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getById(id));
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(
            @RequestBody LoanRequest request) {

        return ResponseEntity.ok(loanService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> update(
            @PathVariable Long id,
            @RequestBody LoanRequest request) {

        return ResponseEntity.ok(loanService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}