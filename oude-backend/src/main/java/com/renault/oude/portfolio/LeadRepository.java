package com.renault.oude.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, String> {
    List<Lead> findByPortfolioId(String portfolioId);
    void deleteByPortfolioId(String portfolioId);
}
