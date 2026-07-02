package com.renault.oude.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AffaireRepository extends JpaRepository<Affaire, String> {
    List<Affaire> findByPortefeuilleId(String portefeuilleId);
    void deleteByPortefeuilleId(String portefeuilleId);
}
