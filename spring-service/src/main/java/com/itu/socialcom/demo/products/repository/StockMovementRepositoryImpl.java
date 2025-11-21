package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.StockMovement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockMovementRepositoryImpl implements StockMovementRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<StockMovement> findByMultipleCriteria(
            Long idSeller,
            String search,
            String movementType,
            Long productId,
            Long variantId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StockMovement> query = cb.createQuery(StockMovement.class);
        Root<StockMovement> root = query.from(StockMovement.class);

        List<Predicate> predicates = buildPredicates(cb, root, idSeller, search, movementType, productId, variantId, startDate, endDate);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(root.get("actionAt")));

        TypedQuery<StockMovement> typedQuery = entityManager.createQuery(query);
        
        // Apply pagination
        int totalSize = typedQuery.getResultList().size();
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<StockMovement> results = typedQuery.getResultList();
        
        return new PageImpl<>(results, pageable, totalSize);
    }

    @Override
    public List<StockMovement> findAllByMultipleCriteria(
            Long idSeller,
            String search,
            String movementType,
            Long productId,
            Long variantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StockMovement> query = cb.createQuery(StockMovement.class);
        Root<StockMovement> root = query.from(StockMovement.class);

        List<Predicate> predicates = buildPredicates(cb, root, idSeller, search, movementType, productId, variantId, startDate, endDate);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(root.get("actionAt")));

        TypedQuery<StockMovement> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<StockMovement> root,
            Long idSeller,
            String search,
            String movementType,
            Long productId,
            Long variantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Predicate> predicates = new ArrayList<>();

        // Always filter by seller
        predicates.add(cb.equal(root.get("idSeller"), idSeller));

        // Add search predicate if search term is provided
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toUpperCase() + "%";
            
            Predicate searchPredicate = cb.or(
                cb.like(cb.upper(root.get("productName")), searchPattern),
                cb.like(cb.upper(root.get("variantName")), searchPattern),
                cb.like(cb.upper(root.get("skuPrefix")), searchPattern),
                cb.like(cb.upper(root.get("variantSku")), searchPattern)
            );
            predicates.add(searchPredicate);
        }

        // Add movement type filter if provided
        if (movementType != null && !movementType.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("movementType"), movementType));
        }

        // Add product ID filter if provided
        if (productId != null) {
            predicates.add(cb.equal(root.get("idProduct"), productId));
        }

        // Add variant ID filter if provided
        if (variantId != null) {
            predicates.add(cb.equal(root.get("idVariant"), variantId));
        }

        // Add date range filter if both dates are provided
        if (startDate != null && endDate != null) {
            predicates.add(cb.between(root.get("actionAt"), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("actionAt"), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("actionAt"), endDate));
        }

        return predicates;
    }
}