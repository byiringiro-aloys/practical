package com.erp.Enterprise_Resource_Planning.service;

import com.erp.Enterprise_Resource_Planning.dto.DeductionRequest;
import com.erp.Enterprise_Resource_Planning.dto.DeductionResponse;
import com.erp.Enterprise_Resource_Planning.entity.Deduction;
import com.erp.Enterprise_Resource_Planning.exception.DuplicateResourceException;
import com.erp.Enterprise_Resource_Planning.exception.ResourceNotFoundException;
import com.erp.Enterprise_Resource_Planning.repository.DeductionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeductionService {

    private final DeductionRepository deductionRepository;

    public DeductionService(DeductionRepository deductionRepository) {
        this.deductionRepository = deductionRepository;
    }

    @Transactional
    public DeductionResponse create(DeductionRequest request) {
        if (deductionRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Deduction already exists: " + request.getName());
        }
        Deduction d = Deduction.builder()
                .name(request.getName())
                .percentage(request.getPercentage())
                .build();
        return toResponse(deductionRepository.save(d));
    }

    @Transactional(readOnly = true)
    public List<DeductionResponse> getAll() {
        return deductionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeductionResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public DeductionResponse update(Long id, DeductionRequest request) {
        Deduction d = find(id);
        // Allow same name (no-op rename), but reject if new name belongs to another record
        deductionRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("Deduction name already in use: " + request.getName());
                    }
                });
        d.setName(request.getName());
        d.setPercentage(request.getPercentage());
        return toResponse(deductionRepository.save(d));
    }

    @Transactional
    public void delete(Long id) {
        deductionRepository.delete(find(id));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Deduction find(Long id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found: " + id));
    }

    private DeductionResponse toResponse(Deduction d) {
        return DeductionResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .percentage(d.getPercentage())
                .build();
    }
}
