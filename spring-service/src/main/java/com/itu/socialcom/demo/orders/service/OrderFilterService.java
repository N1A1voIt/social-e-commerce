package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.orders.OrderParent;
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
public class OrderFilterService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<OrderParent> findOrdersWithFilters(
            Integer idSeller,
            Integer status,
            String customerName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String idPc,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Query for getting the results
        CriteriaQuery<OrderParent> query = cb.createQuery(OrderParent.class);
        Root<OrderParent> order = query.from(OrderParent.class);
        
        // Build predicates dynamically
        List<Predicate> predicates = buildPredicates(cb, order, idSeller, status, customerName, startDate, endDate, idPc);
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(order.get("createdAt")));
        
        // Create typed query with pagination
        TypedQuery<OrderParent> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<OrderParent> results = typedQuery.getResultList();
        
        // Get total count
        long total = countOrdersWithFilters(idSeller, status, customerName, startDate, endDate, idPc);
        
        return new PageImpl<>(results, pageable, total);
    }

    public long countOrdersWithFilters(
            Integer idSeller,
            Integer status,
            String customerName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String idPc) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Query for counting
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OrderParent> order = countQuery.from(OrderParent.class);
        
        // Build predicates dynamically
        List<Predicate> predicates = buildPredicates(cb, order, idSeller, status, customerName, startDate, endDate, idPc);
        
        countQuery.select(cb.count(order));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<OrderParent> order,
            Integer idSeller,
            Integer status,
            String customerName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String idPc) {

        List<Predicate> predicates = new ArrayList<>();
        
        // Always filter by seller
        predicates.add(cb.equal(order.get("idSeller"), idSeller));
        
        // Optional: Filter by status
        if (status != null) {
            predicates.add(cb.equal(order.get("dStatus"), status));
        }
        
        // Optional: Filter by customer name (case-insensitive partial match)
        if (customerName != null && !customerName.trim().isEmpty()) {
            Expression<String> customerNameExpr = order.get("dCustomerName");
            // Handle potential null values in database
            Predicate customerNameNotNull = cb.isNotNull(customerNameExpr);
            Predicate customerNameLike = cb.like(
                cb.lower(customerNameExpr),
                "%" + customerName.toLowerCase().trim() + "%"
            );
            predicates.add(cb.and(customerNameNotNull, customerNameLike));
        }
        
        // Optional: Filter by start date
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(order.get("createdAt"), startDate));
        }
        
        // Optional: Filter by end date
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(order.get("createdAt"), endDate));
        }
        
        // Optional: Filter by potential customer ID (idPc)
        if (idPc != null && !idPc.trim().isEmpty()) {
            predicates.add(cb.equal(order.get("idPc"), idPc));
        }
        
        return predicates;
    }
}
