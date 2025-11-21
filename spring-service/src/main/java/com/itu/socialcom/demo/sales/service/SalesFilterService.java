package com.itu.socialcom.demo.sales.service;

import com.itu.socialcom.demo.sales.Sales;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesFilterService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Sales> findSalesWithFilters(
            Integer idSeller,
            Integer status,
            String fromName,
            String orderId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Query for getting the results
        CriteriaQuery<Sales> query = cb.createQuery(Sales.class);
        Root<Sales> sale = query.from(Sales.class);
        
        // Build predicates dynamically
        List<Predicate> predicates = buildPredicates(cb, sale, idSeller, status, fromName, orderId, startDate, endDate);
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(sale.get("effectuatedAt")));
        
        // Create typed query with pagination
        TypedQuery<Sales> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Sales> results = typedQuery.getResultList();
        
        // Get total count
        long total = countSalesWithFilters(idSeller, status, fromName, orderId, startDate, endDate);
        
        return new PageImpl<>(results, pageable, total);
    }

    public long countSalesWithFilters(
            Integer idSeller,
            Integer status,
            String fromName,
            String orderId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Query for counting
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Sales> sale = countQuery.from(Sales.class);
        
        // Build predicates dynamically
        List<Predicate> predicates = buildPredicates(cb, sale, idSeller, status, fromName, orderId, startDate, endDate);
        
        countQuery.select(cb.count(sale));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Sales> sale,
            Integer idSeller,
            Integer status,
            String fromName,
            String orderId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Predicate> predicates = new ArrayList<>();
        
        // Always filter by seller
        predicates.add(cb.equal(sale.get("idSeller"), idSeller));
        
        // Optional: Filter by status
        if (status != null) {
            predicates.add(cb.equal(sale.get("status"), status));
        }
        
        // Optional: Filter by from name (case-insensitive partial match)
        if (fromName != null && !fromName.trim().isEmpty()) {
            Expression<String> fromNameExpr = sale.get("fromName");
            // Handle potential null values in database
            Predicate fromNameNotNull = cb.isNotNull(fromNameExpr);
            Predicate fromNameLike = cb.like(
                cb.lower(fromNameExpr),
                "%" + fromName.toLowerCase().trim() + "%"
            );
            predicates.add(cb.and(fromNameNotNull, fromNameLike));
        }
        
        // Optional: Filter by order ID
        if (orderId != null && !orderId.trim().isEmpty()) {
            try {
                Integer orderIdInt = Integer.parseInt(orderId.trim());
                predicates.add(cb.equal(sale.get("idOrderM"), orderIdInt));
            } catch (NumberFormatException e) {
                // If orderId is not a valid number, ignore this filter
            }
        }
        
        // Optional: Filter by start date
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(sale.get("effectuatedAt"), startDate));
        }
        
        // Optional: Filter by end date
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(sale.get("effectuatedAt"), endDate));
        }
        
        return predicates;
    }
    public List<Sales> filterSales(
            Integer idSeller,
            Integer status,
            String fromName,
            String orderId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Sales> query = cb.createQuery(Sales.class);
        Root<Sales> sale = query.from(Sales.class);
        
        // Build predicates dynamically
        List<Predicate> predicates = buildPredicates(cb, sale, idSeller, status, fromName, orderId, startDate, endDate);
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(sale.get("effectuatedAt")));
        
        TypedQuery<Sales> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }}
