package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
