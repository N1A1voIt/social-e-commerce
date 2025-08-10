package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that VariantRepository interface is properly defined with all required methods.
 * This test validates the interface structure without requiring database setup.
 * 
 * Requirements tested:
 * - 2.1: Variant creation and management
 * - 2.6: Variant listing for products  
 * - 2.7: Cascade deletion of variants when products are deleted
 */
class VariantRepositoryInterfaceTest {

    @Test
    void testVariantRepositoryExtendsJpaRepository() {
        // Verify that VariantRepository extends JpaRepository
        assertThat(JpaRepository.class.isAssignableFrom(VariantRepository.class)).isTrue();
    }

    @Test
    void testFindByIdProductMethodExists() throws NoSuchMethodException {
        // Verify findByIdProduct method exists with correct signature
        // Requirement 2.6: WHEN a seller views product variants THEN the system SHALL display all variants for a specific product
        Method method = VariantRepository.class.getMethod("findByIdProduct", Long.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    void testDeleteByIdProductMethodExists() throws NoSuchMethodException {
        // Verify deleteByIdProduct method exists with correct signature
        // Requirement 2.7: WHEN a product is deleted THEN the system SHALL cascade delete all associated variants
        Method method = VariantRepository.class.getMethod("deleteByIdProduct", Long.class);
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }

    @Test
    void testFindByIdVariantAndIdProductMethodExists() throws NoSuchMethodException {
        // Verify findByIdVariantAndIdProduct method exists with correct signature
        // Requirement 2.1: Support for variant-specific operations with product validation
        Method method = VariantRepository.class.getMethod("findByIdVariantAndIdProduct", Long.class, Long.class);
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    void testExistsByIdVariantAndIdProductMethodExists() throws NoSuchMethodException {
        // Verify existsByIdVariantAndIdProduct method exists with correct signature
        // Requirement 2.1: Variant validation and existence checking
        Method method = VariantRepository.class.getMethod("existsByIdVariantAndIdProduct", Long.class, Long.class);
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    void testCountByIdProductMethodExists() throws NoSuchMethodException {
        // Verify countByIdProduct method exists with correct signature
        // Requirement 2.6: Support for variant management and statistics
        Method method = VariantRepository.class.getMethod("countByIdProduct", Long.class);
        assertThat(method.getReturnType()).isEqualTo(long.class);
    }

    @Test
    void testFindByIdProductOrderByCreatedAtDescMethodExists() throws NoSuchMethodException {
        // Verify findByIdProductOrderByCreatedAtDesc method exists with correct signature
        // Requirement 2.6: Organized variant display for better user experience
        Method method = VariantRepository.class.getMethod("findByIdProductOrderByCreatedAtDesc", Long.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    void testFindByIdProductOrderByPriceAscMethodExists() throws NoSuchMethodException {
        // Verify findByIdProductOrderByPriceAsc method exists with correct signature
        // Requirement 2.6: Support for price-based variant organization
        Method method = VariantRepository.class.getMethod("findByIdProductOrderByPriceAsc", Long.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    void testAllRequiredMethodsArePresent() {
        // Get all declared methods
        Method[] methods = VariantRepository.class.getDeclaredMethods();
        
        // Verify we have the expected number of custom methods (7 custom methods)
        assertThat(methods.length).isGreaterThanOrEqualTo(7);
        
        // Verify method names are present
        String[] expectedMethods = {
            "findByIdProduct",
            "deleteByIdProduct",
            "findByIdVariantAndIdProduct",
            "existsByIdVariantAndIdProduct",
            "countByIdProduct",
            "findByIdProductOrderByCreatedAtDesc",
            "findByIdProductOrderByPriceAsc"
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
                .withFailMessage("Method %s not found in VariantRepository", expectedMethod)
                .isTrue();
        }
    }

    @Test
    void testRequiredMethodsFromTaskSpecification() {
        // Verify the three specific methods mentioned in the task are present
        // Task requirement: Implement findByIdProduct and deleteByIdProduct methods
        // Task requirement: Add findByIdVariantAndIdProduct for specific variant queries
        
        Method[] methods = VariantRepository.class.getDeclaredMethods();
        String[] taskRequiredMethods = {
            "findByIdProduct",
            "deleteByIdProduct", 
            "findByIdVariantAndIdProduct"
        };
        
        for (String requiredMethod : taskRequiredMethods) {
            boolean methodFound = false;
            for (Method method : methods) {
                if (method.getName().equals(requiredMethod)) {
                    methodFound = true;
                    break;
                }
            }
            assertThat(methodFound)
                .withFailMessage("Task-required method %s not found in VariantRepository", requiredMethod)
                .isTrue();
        }
    }
}