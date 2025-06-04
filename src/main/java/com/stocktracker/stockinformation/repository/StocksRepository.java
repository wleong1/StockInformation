package com.stocktracker.stockinformation.repository;

import com.stocktracker.stockinformation.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StocksRepository extends JpaRepository<Stock, Long> {

}
