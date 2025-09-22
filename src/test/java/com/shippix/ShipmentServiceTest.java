package com.shippix;

import com.shippix.Order.DTO.ShipmentResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.Shipment;
import com.shippix.Order.Model.Truck;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Repo.ShipmentRepo;
import com.shippix.Order.Repo.TruckRepo;
import com.shippix.Order.Repo.WarehouseRepo;
import com.shippix.Order.Service.ShipmentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepo shipmentRepo;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private TruckRepo truckRepo;

    @Mock
    private WarehouseRepo warehouseRepo;

    @InjectMocks
    private ShipmentService shipmentService;

    private Warehouse warehouse;
    private Truck truck;
    private Order order;
    private Shipment shipment;
    private List<Long> orderIds;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setWarehouseId(1L);
        warehouse.setCapacityWeight(1000.0);
        warehouse.setCurrentOccupancyWeight(500.0);
        warehouse.setStatus(Warehouse.Status.ACTIVE);

        truck = new Truck();
        truck.setTruckId(1L);
        truck.setStatus(Truck.Status.AVAILABLE);

        order = new Order();
        order.setOrderId(1L);
        order.setPackageWeight(100.0);
        order.setStatus(Order.Status.IN_PROGRESS);
        order.setAssignedWarehouse(warehouse);

        orders = List.of(order);
        orderIds = List.of(1L);

        shipment = new Shipment();
        shipment.setShipmentId(1L);
        shipment.setWarehouse(warehouse);
        shipment.setTruck(truck);
        shipment.setOrders(orders);
        shipment.setType(Shipment.Type.PICKUP);
        shipment.setStatus(Shipment.Status.PLANNED);
    }

    // Tests for createShipment
    @Test
    void createShipment_Pickup_Success() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));
        when(truckRepo.findById(1L)).thenReturn(Optional.of(truck));
        when(orderRepo.findAllById(orderIds)).thenReturn(orders);
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        ShipmentResponse response = shipmentService.createShipment(1L, 1L, orderIds, Shipment.Type.PICKUP);

        assertNotNull(response);
        assertEquals(Shipment.Status.PLANNED, shipment.getStatus());
        assertEquals(Truck.Status.IN_TRANSIT, truck.getStatus());
        assertEquals(Order.Status.IN_PROGRESS, orders.get(0).getStatus());
        verify(shipmentRepo).save(any(Shipment.class));
        verify(truckRepo).save(truck);
        verify(orderRepo).saveAll(orders);
    }

    @Test
    void createShipment_Delivery_Success() {
        shipment.setType(Shipment.Type.DELIVERY);
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));
        when(truckRepo.findById(1L)).thenReturn(Optional.of(truck));
        when(orderRepo.findAllById(orderIds)).thenReturn(orders);
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        ShipmentResponse response = shipmentService.createShipment(1L, 1L, orderIds, Shipment.Type.DELIVERY);

        assertNotNull(response);
        assertEquals(Shipment.Status.PLANNED, shipment.getStatus());
        assertEquals(Truck.Status.IN_TRANSIT, truck.getStatus());
        assertEquals(Order.Status.OUT_FOR_DELIVERY, orders.get(0).getStatus());
        verify(shipmentRepo).save(any(Shipment.class));
        verify(truckRepo).save(truck);
        verify(orderRepo).saveAll(orders);
    }

    @Test
    void createShipment_WarehouseNotFound_ThrowsException() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shipmentService.createShipment(1L, 1L, orderIds, Shipment.Type.PICKUP));

        assertEquals("Warehouse not found", exception.getMessage());
        verify(warehouseRepo).findById(1L);
        verifyNoInteractions(truckRepo, orderRepo, shipmentRepo);
    }

    @Test
    void createShipment_TruckNotFound_ThrowsException() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));
        when(truckRepo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shipmentService.createShipment(1L, 1L, orderIds, Shipment.Type.PICKUP));

        assertEquals("Truck not found", exception.getMessage());
        verify(warehouseRepo).findById(1L);
        verify(truckRepo).findById(1L);
        verifyNoInteractions(orderRepo, shipmentRepo);
    }

    @Test
    void createShipment_TruckNotAvailable_ThrowsException() {
        truck.setStatus(Truck.Status.IN_TRANSIT);
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));
        when(truckRepo.findById(1L)).thenReturn(Optional.of(truck));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> shipmentService.createShipment(1L, 1L, orderIds, Shipment.Type.PICKUP));

        assertEquals("Truck not available for shipment", exception.getMessage());
        verify(warehouseRepo).findById(1L);
        verify(truckRepo).findById(1L);
        verifyNoInteractions(orderRepo, shipmentRepo);
    }

    @Test
    void createShipment_NoValidOrders_ThrowsException() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));
        when(truckRepo.findById(1L)).thenReturn(Optional.of(truck));
        when(orderRepo.findAllById(orderIds)).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> shipmentService.createShipment(1L, 1L, orderIds, Shipment.Type.PICKUP));

        assertEquals("No valid orders provided", exception.getMessage());
        verify(warehouseRepo).findById(1L);
        verify(truckRepo).findById(1L);
        verify(orderRepo).findAllById(orderIds);
        verifyNoInteractions(shipmentRepo);
    }

    // Tests for startShipment
    @Test
    void startShipment_Success() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);

        ShipmentResponse response = shipmentService.startShipment(1L);

        assertNotNull(response);
        assertEquals(Shipment.Status.IN_PROGRESS, shipment.getStatus());
        assertNotNull(shipment.getStartedAt());
        verify(shipmentRepo).save(shipment);
    }

    @Test
    void startShipment_ShipmentNotFound_ThrowsException() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shipmentService.startShipment(1L));

        assertEquals("Shipment not found", exception.getMessage());
        verify(shipmentRepo).findById(1L);
        verifyNoMoreInteractions(shipmentRepo);
    }

    @Test
    void startShipment_NotPlanned_ThrowsException() {
        shipment.setStatus(Shipment.Status.IN_PROGRESS);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> shipmentService.startShipment(1L));

        assertEquals("Shipment not in PLANNED state", exception.getMessage());
        verify(shipmentRepo).findById(1L);
        verifyNoMoreInteractions(shipmentRepo);
    }

    // Tests for completeShipment
    @Test
    void completeShipment_Pickup_Success() {
        shipment.setStatus(Shipment.Status.IN_PROGRESS);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(warehouseRepo.save(any(Warehouse.class))).thenReturn(warehouse);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        ShipmentResponse response = shipmentService.completeShipment(1L);

        assertNotNull(response);
        assertEquals(Shipment.Status.COMPLETED, shipment.getStatus());
        assertEquals(Truck.Status.AVAILABLE, truck.getStatus());
        assertEquals(Order.Status.IN_WAREHOUSE, orders.get(0).getStatus());
        assertNotNull(shipment.getCompletedAt());
        verify(warehouseRepo).save(warehouse);
        verify(truckRepo).save(truck);
        verify(orderRepo).saveAll(orders);
    }

    @Test
    void completeShipment_Delivery_Success() {
        shipment.setType(Shipment.Type.DELIVERY);
        shipment.setStatus(Shipment.Status.IN_PROGRESS);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(warehouseRepo.save(any(Warehouse.class))).thenReturn(warehouse);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        ShipmentResponse response = shipmentService.completeShipment(1L);

        assertNotNull(response);
        assertEquals(Shipment.Status.COMPLETED, shipment.getStatus());
        assertEquals(Truck.Status.AVAILABLE, truck.getStatus());
        assertEquals(Order.Status.DELIVERED, orders.get(0).getStatus());
        assertNull(orders.get(0).getAssignedWarehouse());
        assertNotNull(shipment.getCompletedAt());
        verify(warehouseRepo).save(warehouse);
        verify(truckRepo).save(truck);
        verify(orderRepo).saveAll(orders);
    }

    @Test
    void completeShipment_WarehouseFull_AfterPickup() {
        shipment.setStatus(Shipment.Status.IN_PROGRESS);
        warehouse.setCurrentOccupancyWeight(950.0); // Close to capacity
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(warehouseRepo.save(any(Warehouse.class))).thenReturn(warehouse);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        shipmentService.completeShipment(1L);

        assertEquals(Warehouse.Status.FULL, warehouse.getStatus());
        verify(warehouseRepo).save(warehouse);
    }

    @Test
    void completeShipment_WarehouseNotFull_AfterDelivery() {
        shipment.setType(Shipment.Type.DELIVERY);
        shipment.setStatus(Shipment.Status.IN_PROGRESS);
        warehouse.setStatus(Warehouse.Status.FULL);
        warehouse.setCurrentOccupancyWeight(1000.0);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(warehouseRepo.save(any(Warehouse.class))).thenReturn(warehouse);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        shipmentService.completeShipment(1L);

        assertEquals(Warehouse.Status.ACTIVE, warehouse.getStatus());
        verify(warehouseRepo).save(warehouse);
    }

    @Test
    void completeShipment_ShipmentNotFound_ThrowsException() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shipmentService.completeShipment(1L));

        assertEquals("Shipment not found", exception.getMessage());
        verify(shipmentRepo).findById(1L);
        verifyNoInteractions(warehouseRepo, truckRepo, orderRepo);
    }

    @Test
    void completeShipment_NotInProgress_ThrowsException() {
        shipment.setStatus(Shipment.Status.PLANNED);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> shipmentService.completeShipment(1L));

        assertEquals("Shipment not in progress", exception.getMessage());
        verify(shipmentRepo).findById(1L);
        verifyNoInteractions(warehouseRepo, truckRepo, orderRepo);
    }

    // Tests for cancelShipment
    @Test
    void cancelShipment_Pickup_Success() {
        shipment.setStatus(Shipment.Status.PLANNED);
        truck.setStatus(Truck.Status.IN_TRANSIT);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        ShipmentResponse response = shipmentService.cancelShipment(1L);

        assertNotNull(response);
        assertEquals(Shipment.Status.CANCELED, shipment.getStatus());
        assertEquals(Truck.Status.AVAILABLE, truck.getStatus());
        assertEquals(Order.Status.IN_PROGRESS, orders.get(0).getStatus());
        verify(truckRepo).save(truck);
        verify(orderRepo).saveAll(orders);
    }

    @Test
    void cancelShipment_Delivery_Success() {
        shipment.setType(Shipment.Type.DELIVERY);
        shipment.setStatus(Shipment.Status.PLANNED);
        truck.setStatus(Truck.Status.IN_TRANSIT);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(any(Shipment.class))).thenReturn(shipment);
        when(truckRepo.save(any(Truck.class))).thenReturn(truck);
        when(orderRepo.saveAll(anyList())).thenReturn(orders);

        ShipmentResponse response = shipmentService.cancelShipment(1L);

        assertNotNull(response);
        assertEquals(Shipment.Status.CANCELED, shipment.getStatus());
        assertEquals(Truck.Status.AVAILABLE, truck.getStatus());
        assertEquals(Order.Status.IN_WAREHOUSE, orders.get(0).getStatus());
        verify(truckRepo).save(truck);
        verify(orderRepo).saveAll(orders);
    }

    @Test
    void cancelShipment_ShipmentNotFound_ThrowsException() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shipmentService.cancelShipment(1L));

        assertEquals("Shipment not found", exception.getMessage());
        verify(shipmentRepo).findById(1L);
        verifyNoInteractions(truckRepo, orderRepo);
    }

    @Test
    void cancelShipment_Completed_ThrowsException() {
        shipment.setStatus(Shipment.Status.COMPLETED);
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> shipmentService.cancelShipment(1L));

        assertEquals("Cannot cancel a completed shipment", exception.getMessage());
        verify(shipmentRepo).findById(1L);
        verifyNoInteractions(truckRepo, orderRepo);
    }

    // Tests for getAllShipments
    @Test
    void getAllShipments_Success() {
        when(shipmentRepo.findAll()).thenReturn(List.of(shipment));

        List<ShipmentResponse> responses = shipmentService.getAllShipments();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(shipmentRepo).findAll();
    }

    @Test
    void getAllShipments_EmptyList() {
        when(shipmentRepo.findAll()).thenReturn(Collections.emptyList());

        List<ShipmentResponse> responses = shipmentService.getAllShipments();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(shipmentRepo).findAll();
    }

    // Tests for getShipmentById
    @Test
    void getShipmentById_Success() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentResponse response = shipmentService.getShipmentById(1L);

        assertNotNull(response);
        verify(shipmentRepo).findById(1L);
    }

    @Test
    void getShipmentById_ShipmentNotFound_ThrowsException() {
        when(shipmentRepo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shipmentService.getShipmentById(1L));

        assertEquals("Shipment not found", exception.getMessage());
        verify(shipmentRepo).findById(1L);
    }
}