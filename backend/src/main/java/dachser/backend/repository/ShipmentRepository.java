package dachser.backend.repository;

import dachser.backend.entity.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link Shipment}.
 * Basic CRUD operations are provided by {@link JpaRepository}.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Returns all shipments with their customer loaded in the same query
     * (avoids one extra query per shipment).
     */
    @Override
    @EntityGraph(attributePaths = "customer")
    List<Shipment> findAll();

    /**
     * Finds a shipment by its business code (e.g. {@code SHP-0001}).
     */
    @EntityGraph(attributePaths = "customer")
    Optional<Shipment> findByShipmentCode(String shipmentCode);
}
