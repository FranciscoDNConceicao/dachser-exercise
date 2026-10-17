package dachser.backend.repository;

import dachser.backend.entity.Cost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data access for {@link Cost}.
 * Basic CRUD operations are provided by {@link JpaRepository}.
 */
@Repository
public interface CostRepository extends JpaRepository<Cost, Long> {

    /**
     * Returns all costs recorded for a shipment.
     */
    List<Cost> findByShipmentId(Long shipmentId);

    /**
     * Sums all costs of a shipment in the database.
     *
     * @return the total costs, or zero when the shipment has no costs
     */
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Cost c WHERE c.shipment.id = :shipmentId")
    BigDecimal sumAmountByShipmentId(@Param("shipmentId") Long shipmentId);
}
