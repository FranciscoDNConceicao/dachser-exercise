package dachser.backend.repository;

import dachser.backend.entity.ProfitCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link ProfitCalculation}.
 * Basic CRUD operations are provided by {@link JpaRepository}.
 */
@Repository
public interface ProfitCalculationRepository extends JpaRepository<ProfitCalculation, Long> {

    /**
     * Returns the calculation history of a shipment, most recent first.
     */
    List<ProfitCalculation> findByShipmentIdOrderByCalculatedAtDesc(Long shipmentId);

    /**
     * Finds a calculation only if it belongs to the given shipment
     * (a calculation cannot be reached through another shipment).
     */
    Optional<ProfitCalculation> findByIdAndShipmentId(Long id, Long shipmentId);
}
