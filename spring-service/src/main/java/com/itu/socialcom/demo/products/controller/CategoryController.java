package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.model.Category;
import com.itu.socialcom.demo.products.repository.CategoryRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TokenV2ServiceImpl tokenService;

    /**
     * Get all categories - simpler endpoint without complex validation
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get all categories with authentication
     */
    @GetMapping("/categories/auth")
    public ResponseEntity<?> getAllCategoriesAuth(@RequestHeader("Authorization") String token) {
        try {
            // Validate token
            token = token.replace("Bearer ", "");
            Seller seller = tokenService.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse response = new ApiResponse();
                response.setStatus(401);
                return ResponseEntity.status(401).body(response);
            }

            List<Category> categories = categoryRepository.findAll();
            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get category by ID
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            // Validate token
            token = token.replace("Bearer ", "");
            Seller seller = tokenService.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse response = new ApiResponse();
                response.setStatus(401);
                return ResponseEntity.status(401).body(response);
            }

            Optional<Category> category = categoryRepository.findById(id);
            if (category.isEmpty()) {
                ApiResponse response = new ApiResponse();
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }

            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            response.setData(category.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Create a new category
     */
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestHeader("Authorization") String token, @RequestBody Category category) {
        try {
            // Validate token
            token = token.replace("Bearer ", "");
            Seller seller = tokenService.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse response = new ApiResponse();
                response.setStatus(401);
                return ResponseEntity.status(401).body(response);
            }

            Category savedCategory = categoryRepository.save(category);
            ApiResponse response = new ApiResponse();
            response.setStatus(201);
            response.setData(savedCategory);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Update an existing category
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Category categoryDetails) {
        try {
            // Validate token
            token = token.replace("Bearer ", "");
            Seller seller = tokenService.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse response = new ApiResponse();
                response.setStatus(401);
                return ResponseEntity.status(401).body(response);
            }

            Optional<Category> optionalCategory = categoryRepository.findById(id);
            if (optionalCategory.isEmpty()) {
                ApiResponse response = new ApiResponse();
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }

            Category category = optionalCategory.get();
            
            // Update fields
            if (categoryDetails.getVal() != null) {
                category.setVal(categoryDetails.getVal());
            }
            if (categoryDetails.getDescription() != null) {
                category.setDescription(categoryDetails.getDescription());
            }
            
            Category updatedCategory = categoryRepository.save(category);
            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            response.setData(updatedCategory);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete a category
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            // Validate token
            token = token.replace("Bearer ", "");
            Seller seller = tokenService.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse response = new ApiResponse();
                response.setStatus(401);
                return ResponseEntity.status(401).body(response);
            }

            Optional<Category> category = categoryRepository.findById(id);
            if (category.isEmpty()) {
                ApiResponse response = new ApiResponse();
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }

            categoryRepository.deleteById(id);
            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }
}