package com.altis.library.publishers.controllers;

import com.altis.library.publishers.models.dtos.PublisherRequest;
import com.altis.library.publishers.models.dtos.PublisherResponse;
import com.altis.library.publishers.services.PublisherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    public ResponseEntity<List<PublisherResponse>> findAll() {
        return ResponseEntity.ok(publisherService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(publisherService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PublisherResponse> save(
            @Valid @RequestBody PublisherRequest request) {

        return ResponseEntity.ok(publisherService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PublisherRequest request) {

        return ResponseEntity.ok(publisherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        publisherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}