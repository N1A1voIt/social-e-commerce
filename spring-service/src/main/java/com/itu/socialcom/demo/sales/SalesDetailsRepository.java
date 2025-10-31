package com.itu.socialcom.demo.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesDetailsRepository extends JpaRepository<SalesDetails, Integer> {
}
