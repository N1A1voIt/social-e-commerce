package com.itu.socialcom.demo.socialmedia.controller;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import com.itu.socialcom.demo.socialmedia.entity.VMpNumbers;
import com.itu.socialcom.demo.socialmedia.repository.VMpNumbersRepository;
import com.itu.socialcom.demo.socialmedia.service.VMpNumbersService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vmpnumbers")
public class VMpNumbersController {

    private final VMpNumbersService service;
    @Autowired
    private VMpNumbersRepository vMpNumbersRepository;
    private final TokenV2ServiceImpl tokenV2Service;

    public VMpNumbersController(VMpNumbersService service,TokenV2ServiceImpl tokenV2Service) {
        this.service = service;
        this.tokenV2Service = tokenV2Service;
    }

    @GetMapping("/fetch-numbers/{id_mp}")
    public ResponseEntity<ApiResponse> getNumbersByManagedPage(@PathVariable Long id_mp, @RequestHeader("Authorization") String token) {
        try{
            Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(vMpNumbersRepository.findByIdMp(id_mp));
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(null);
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VMpNumbers> getById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
            Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));
            return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ManagedPagesNumber create(@RequestBody ManagedPagesNumber vMpNumbers, @RequestHeader("Authorization") String token) {
            Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));
            return service.save(vMpNumbers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManagedPagesNumber> update(@PathVariable Long id, @RequestBody ManagedPagesNumber vMpNumbers, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));
            return ResponseEntity.ok(service.update(id, vMpNumbers));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}