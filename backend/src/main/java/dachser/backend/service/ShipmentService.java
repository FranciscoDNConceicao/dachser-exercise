package dachser.backend.service;

import dachser.backend.dto.ShipmentResponse;

import java.util.List;

/**
 * Read operations on shipments, used to search and select the shipment
 * for which the profit is calculated.
 */
public interface ShipmentService {

    /**
     * Returns all shipments.
     */
    List<ShipmentResponse> findAll();

    /**
     * Returns the shipment with the given business code.
     *
     * @throws dachser.backend.exception.ShipmentNotFoundException if it does not exist
     */
    ShipmentResponse findByCode(String shipmentCode);
}
