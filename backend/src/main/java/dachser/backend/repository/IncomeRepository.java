package dachser.backend.repository;

import dachser.backend.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@link Income}.
 * Basic CRUD operations are provided by {@link JpaRepository}; incomes are normally
 * saved and deleted through their {@link dachser.backend.entity.ProfitCalculation} (cascade).
 */
@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
}
