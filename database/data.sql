-- =========================================================
-- Profit Calculation - sample data (foreign keys resolved by business keys)
-- =========================================================

INSERT INTO customers (name, tax_number, email)
VALUES
    ('Atlantic Trading Lda', 'PT500000001', 'finance@atlantic-trading.example'),
    ('Iberian Goods SL', 'ESB00000002', 'accounts@iberian-goods.example'),
    ('Northern Foods Lda', 'PT500000003', 'billing@northern-foods.example');

INSERT INTO shipments (customer_id, shipment_code, description)
VALUES
    ((SELECT id FROM customers WHERE tax_number = 'PT500000001'), 'SHP-0001', 'Shipment Porto - Lisbon'),
    ((SELECT id FROM customers WHERE tax_number = 'ESB00000002'), 'SHP-0002', 'Shipment Lisbon - Madrid'),
    ((SELECT id FROM customers WHERE tax_number = 'PT500000003'), 'SHP-0003', 'Shipment Porto - Braga');

INSERT INTO incomes (shipment_id, description, amount)
VALUES
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0001'), 'Customer payment', 1500.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0002'), 'Customer payment', 800.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0003'), 'Customer payment', 500.00);

INSERT INTO costs (shipment_id, description, amount)
VALUES
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0001'), 'Transport', 600.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0001'), 'Fuel', 200.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0002'), 'Transport', 500.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0002'), 'Storage', 400.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0003'), 'Transport', 250.00),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0003'), 'Fuel', 250.00);

INSERT INTO profit_calculations (shipment_id, total_income, total_costs, profit_or_loss, calculated_at)
VALUES
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0001'), 1500.00, 600.00, 900.00, TIMESTAMP '2026-10-01 09:00:00'),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0001'), 1500.00, 800.00, 700.00, TIMESTAMP '2026-10-02 10:30:00'),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0002'), 800.00, 900.00, -100.00, TIMESTAMP '2026-10-03 14:15:00'),
    ((SELECT id FROM shipments WHERE shipment_code = 'SHP-0003'), 500.00, 500.00, 0.00, TIMESTAMP '2026-10-04 16:45:00');
