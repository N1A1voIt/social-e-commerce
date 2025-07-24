package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that ProductRepository interface is properly defined with all required methods.
 * This test validates the interface structure without requiring database setup.
 */
class ProductRepositoryInterfaceTest {

    @Test
    void testProductRepositoryExtendsJpaRepository() {
        // Verify that ProductRepository extends JpaRepository
        assertThat(JpaRepository.class.isAssignableFrom(ProductRepository.class)).isTrue();
    }

    @Test
    void testFindByIdSellerMethodExists() throws NoSuchMethodException {
        // Verify findByIdSeller method exists with correct signature
        Method method = ProductRepository.class.getMethod("findByIdSeller", Integer.class, Pageable.class);
        assertThat(method.getReturnType()).isEqualTo(Page.class);
    }

    @Test
    void testFindByIdSellerAndNameContainingIgnoreCaseMethodExists() throws NoSuchMethodException {
        // Verify findByIdSellerAndNameContainingIgnoreCase method exists with correct signature
        Method method = ProductRepository.class.getMethod("findByIdSellerAndNameContainingIgnoreCase", 
            Integer.class, String.class, Pageable.class);
        assertThat(method.getReturnType()).isEqualTo(Page.class);
    }

    @Test
    void testFindByIdSellerAndPriceBetweenMethodExists() throws NoSuchMethodException {
        // Verify findByIdSellerAndPriceBetween method exists with correct signature
        Method method = ProductRepository.class.getMethod("findByIdSellerAndPriceBetween", 
            Integer.class, BigDecimal.class, BigDecimal.class, Pageable.class);
        assertThat(method.getReturnType()).isEqualTo(Page.class);
    }

    @Test
    void testFindByIdProductAndIdSellerMethodExists() throws NoSuchMethodException {
        // Verify findByIdProductAndIdSeller method exists with correct signature
        Method method = ProductRepository.class.getMethod("findByIdProductAndIdSeller", Long.class, Integer.class);
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    void testExistsByIdProductAndIdSellerMethodExists() throws NoSuchMethodException {
        // Verify existsByIdProductAndIdSeller method exists with correct signature
        Method method = ProductRepository.class.getMethod("existsByIdProductAndIdSeller", Long.class, Integer.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    void testCountByIdSellerMethodExists() throws NoSuchMethodException {
        // Verify countByIdSeller method exists with correct signature
        Method method = ProductRepository.class.getMethod("countByIdSeller", Integer.class);
        assertThat(method.getReturnType()).isEqualTo(long.class);
    }

    @Test
    void testFindBySellerWithNameAndPriceRangeMethodExists() throws NoSuchMethodException {
        // Verify findBySellerWithNameAndPriceRange method exists with correct signature
        Method method = ProductRepository.class.getMethod("findBySellerWithNameAndPriceRange", 
            Integer.class, String.class, BigDecimal.class, BigDecimal.class, Pageable.class);
        assertThat(method.getReturnType()).isEqualTo(Page.class);
    }

    @Test
    void testAllRequiredMethodsArePresent() {
        // Get all declared methods
        Method[] methods = ProductRepository.class.getDeclaredMethods();
        
        // Verify we have the expected number of custom methods (7 custom methods)
        assertThat(methods.length).isGreaterThanOrEqualTo(7);
        
        // Verify method names are present
        String[] expectedMethods = {
            "findByIdSeller",
            "findByIdSellerAndNameContainingIgnoreCase", 
            "findByIdSellerAndPriceBetween",
            "findByIdProductAndIdSeller",
            "existsByIdProductAndIdSeller",
            "countByIdSeller",
            "findBySellerWithNameAndPriceRange"
        };
        
        for (String expectedMethod : expectedMethods) {
            boolean methodFound = false;
            for (Method method : methods) {
                if (method.getName().equals(expectedMethod)) {
                    methodFound = true;
                    break;
                }
            }
            assertThat(methodFound)
                .withFailMessage("Method %s not found in ProductRepository", expectedMethod)
                .isTrue();
        }
    }
}