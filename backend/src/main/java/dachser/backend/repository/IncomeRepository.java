package dachser.backend.repository;

import dachser.backend.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data access for {@link Income}.
 * Basic CRUD operations are provided by {@link JpaRepository}.
 */
@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    /**
     * Returns all incomes recorded for a shipment.
     */
    List<Income> findByShipmentId(Long shipmentId);

    /**
     * Sums all incomes of a shipment in the database.
     *
     * @return the total income, or zero when the shipment has no incomes
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.shipment.id = :shipmentId")
    BigDecimal sumAmountByShipmentId(@Param("shipmentId") Long shipmentId);
}
