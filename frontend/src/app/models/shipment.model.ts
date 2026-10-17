/**
 * Shipment returned by the backend (GET /api/shipments).
 */
export interface Shipment {
  id: number;
  shipmentCode: string;
  description: string;
  customerName: string;
}
