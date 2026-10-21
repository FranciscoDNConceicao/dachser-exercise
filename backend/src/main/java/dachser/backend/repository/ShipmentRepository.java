package dachser.backend.repository;

import dachser.backend.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access for {@link Shipment}.
 * Basic CRUD operations are provided by {@link JpaRepository}.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Finds a shipment by its business code (e.g. {@code SHP-0001}).
     */
    Optional<Shipment> findByShipmentCode(String shipmentCode);
}
