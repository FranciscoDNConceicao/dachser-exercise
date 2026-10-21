package dachser.backend.repository;

import dachser.backend.entity.Cost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@link Cost}.
 * Basic CRUD operations are provided by {@link JpaRepository}; costs are normally
 * saved and deleted through their {@link dachser.backend.entity.ProfitCalculation} (cascade).
 */
@Repository
public interface CostRepository extends JpaRepository<Cost, Long> {
}
