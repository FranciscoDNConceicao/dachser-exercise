package dachser.backend.service.impl;

import dachser.backend.dto.ShipmentResponse;
import dachser.backend.exception.ShipmentNotFoundException;
import dachser.backend.mapper.ShipmentMapper;
import dachser.backend.repository.ShipmentRepository;
import dachser.backend.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link ShipmentService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @Override
    public List<ShipmentResponse> findAll() {
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Override
    public ShipmentResponse findByCode(String shipmentCode) {
        return shipmentRepository.findByShipmentCode(shipmentCode)
                .map(shipmentMapper::toResponse)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentCode));
    }
}
