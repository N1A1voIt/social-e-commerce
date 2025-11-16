package com.itu.socialcom.demo.posts.services;

import com.itu.socialcom.demo.posts.entity.Post;
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
public class PostFilterService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Post> findPostsWithFilters(
            Long sellerId,
            String title,
            String type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        Root<Post> post = query.from(Post.class);
        
        List<Predicate> predicates = buildPredicates(cb, post, sellerId, title, type, startDate, endDate);
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(post.get("createAt")));
        
        TypedQuery<Post> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Post> results = typedQuery.getResultList();
        
        long total = countPostsWithFilters(sellerId, title, type, startDate, endDate);
        
        return new PageImpl<>(results, pageable, total);
    }

    public long countPostsWithFilters(
            Long sellerId,
            String title,
            String type,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Post> post = countQuery.from(Post.class);
        
        List<Predicate> predicates = buildPredicates(cb, post, sellerId, title, type, startDate, endDate);
        
        countQuery.select(cb.count(post));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Post> post,
            Long sellerId,
            String title,
            String type,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Predicate> predicates = new ArrayList<>();
        
        predicates.add(cb.equal(post.get("idSeller"), sellerId));
        
        if (title != null && !title.trim().isEmpty()) {
            Expression<String> descriptionExpr = post.get("description");
            Predicate descriptionNotNull = cb.isNotNull(descriptionExpr);
            Predicate descriptionLike = cb.like(
                cb.lower(descriptionExpr),
                "%" + title.toLowerCase().trim() + "%"
            );
            predicates.add(cb.and(descriptionNotNull, descriptionLike));
        }
        
        if (type != null && !type.trim().isEmpty()) {
            predicates.add(cb.equal(post.get("type"), type));
        }
        
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(post.get("createAt"), startDate));
        }
        
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(post.get("createAt"), endDate));
        }
        
        return predicates;
    }
}
