package com.stocktracker.stockinformation.repository;

import com.stocktracker.stockinformation.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}
