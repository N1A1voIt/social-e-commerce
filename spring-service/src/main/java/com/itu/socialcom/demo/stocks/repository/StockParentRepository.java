package com.itu.socialcom.demo.stocks.repository;

import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockParentRepository extends JpaRepository<StockParent, Long> {

    StockParent findByIdOrderM(Long idOrderM);
}
