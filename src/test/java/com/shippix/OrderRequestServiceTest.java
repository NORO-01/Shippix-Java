package com.shippix;

import com.shippix.Order.DTO.OrderCreateRequest;
import com.shippix.Order.DTO.OrderResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.OrderRequest;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Repo.OrderRequestRepo;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.Order.Service.WarehouseService;
import com.shippix.Order.Service.OrderRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRequestServiceTest {

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private OrderRequestRepo orderRequestRepo;

    @Mock
    private OrderRepo orderRepo;

    @InjectMocks
    private OrderRequestService orderRequestService;

    private BusinessOwner businessOwner;
    private OrderCreateRequest orderCreateRequest;
    private OrderRequest orderRequest;
    private Warehouse warehouse;
    private Order order;

    @BeforeEach
    void setUp() {
        businessOwner = new BusinessOwner();
        businessOwner.setId(1L);
        
        orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.setOrderDescription("Test order");
        orderCreateRequest.setNotesToDriver("Handle with care");
        orderCreateRequest.setPackageWeight(5.0);
        orderCreateRequest.setPackageValue(100.0);
        orderCreateRequest.setFromLatitude(40.7128);
        orderCreateRequest.setFromLongitude(-74.0060);
        orderCreateRequest.setToLatitude(34.0522);
        orderCreateRequest.setToLongitude(-118.2437);
        orderCreateRequest.setCustName("John Doe");
        orderCreateRequest.setCustPhoneNumber("123-456-7890");
        orderCreateRequest.setCustEmail("john@example.com");
        
        orderRequest = new OrderRequest();
        orderRequest.setReqId(1L);
        orderRequest.setBusinessOwner(businessOwner);
        orderRequest.setOrderDescription("Test order");
        orderRequest.setNotesToDriver("Handle with care");
        orderRequest.setPackageWeight(5.0);
        orderRequest.setPackageValue(100.0);
        orderRequest.setFromLatitude(40.7128);
        orderRequest.setFromLongitude(-74.0060);
        orderRequest.setToLatitude(34.0522);
        orderRequest.setToLongitude(-118.2437);
        orderRequest.setCustName("John Doe");
        orderRequest.setCustPhoneNumber("123-456-7890");
        orderRequest.setCustEmail("john@example.com");
        orderRequest.setDeliveryDistance(3940.0);
        orderRequest.setDecision(OrderRequest.Status.PENDING);
        
        warehouse = new Warehouse();
        warehouse.setWarehouseId(1L);
        warehouse.setName("Main Warehouse");
        
        order = new Order();
        order.setOrderId(1L);
        order.setBusinessOwner(businessOwner);
        order.setOrderDescription("Test order");
        order.setNotesToDriver("Handle with care");
        order.setPackageWeight(5.0);
        order.setPackageValue(100.0);
        order.setDeliveryDistance(3940.0);
        order.setFromLatitude(40.7128);
        order.setFromLongitude(-74.0060);
        order.setToLatitude(34.0522);
        order.setToLongitude(-118.2437);
        order.setCustName("John Doe");
        order.setCustPhoneNumber("123-456-7890");
        order.setCustEmail("john@example.com");
        order.setStatus(Order.Status.ACCEPTED);
        order.setAssignedWarehouse(warehouse);
    }

    @Test
    void submitRequest_ValidData_ReturnsSavedOrderRequest() {
        // Arrange
        when(orderRequestRepo.save(any(OrderRequest.class))).thenReturn(orderRequest);
        
        // Act
        OrderRequest result = orderRequestService.submitRequest(businessOwner, orderCreateRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(businessOwner, result.getBusinessOwner());
        assertEquals(orderCreateRequest.getOrderDescription(), result.getOrderDescription());
        assertEquals(OrderRequest.Status.PENDING, result.getDecision());
        assertTrue(result.getDeliveryDistance() > 0);
        
        verify(orderRequestRepo, times(1)).save(any(OrderRequest.class));
    }

    @Test
    void submitRequest_ZeroCoordinates_CalculatesZeroDistance() {
        // Arrange
        orderCreateRequest.setFromLatitude(0.0);
        orderCreateRequest.setFromLongitude(0.0);
        orderCreateRequest.setToLatitude(0.0);
        orderCreateRequest.setToLongitude(0.0);
        
        when(orderRequestRepo.save(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest savedRequest = invocation.getArgument(0);
            savedRequest.setReqId(1L);
            return savedRequest;
        });
        
        // Act
        OrderRequest result = orderRequestService.submitRequest(businessOwner, orderCreateRequest);
        
        // Assert
        assertEquals(0.0, result.getDeliveryDistance(), 0.001);
    }

    @Test
    void approveRequest_ValidPendingRequest_ReturnsOrderResponse() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(warehouseService.assignWarehouse(any(Order.class))).thenReturn(warehouse);
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        
        // Act
        OrderResponse result = orderRequestService.approveRequest(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(OrderRequest.Status.APPROVED, orderRequest.getDecision());
        assertNotNull(orderRequest.getReviewedAt());
        verify(orderRequestRepo, times(1)).save(orderRequest);
        verify(orderRepo, times(1)).save(any(Order.class));
        verify(warehouseService, times(1)).assignWarehouse(any(Order.class));
    }

    @Test
    void approveRequest_RequestNotFound_ThrowsException() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(1L);
        });
        
        assertEquals("Request not found", exception.getMessage());
        verify(orderRequestRepo, never()).save(any());
        verify(orderRepo, never()).save(any());
    }

    @Test
    void approveRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        orderRequest.setDecision(OrderRequest.Status.APPROVED);
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(1L);
        });
        
        assertEquals("Request already processed", exception.getMessage());
        verify(orderRequestRepo, never()).save(any());
        verify(orderRepo, never()).save(any());
    }

    @Test
    void approveRequest_AlreadyRejected_ThrowsException() {
        // Arrange
        orderRequest.setDecision(OrderRequest.Status.REJECTED);
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(1L);
        });
        
        assertEquals("Request already processed", exception.getMessage());
        verify(orderRequestRepo, never()).save(any());
        verify(orderRepo, never()).save(any());
    }

    @Test
    void rejectRequest_ValidPendingRequest_ReturnsRejectedOrderRequest() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(orderRequestRepo.save(orderRequest)).thenReturn(orderRequest);
        
        // Act
        OrderRequest result = orderRequestService.rejectRequest(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(OrderRequest.Status.REJECTED, result.getDecision());
        assertNotNull(result.getReviewedAt());
        verify(orderRequestRepo, times(1)).save(orderRequest);
    }

    @Test
    void rejectRequest_RequestNotFound_ThrowsException() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.rejectRequest(1L);
        });
        
        assertEquals("Request not found", exception.getMessage());
        verify(orderRequestRepo, never()).save(any());
    }

    @Test
    void rejectRequest_AlreadyApproved_ThrowsException() {
        // Arrange
        orderRequest.setDecision(OrderRequest.Status.APPROVED);
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.rejectRequest(1L);
        });
        
        assertEquals("Request already processed", exception.getMessage());
        verify(orderRequestRepo, never()).save(any());
    }

    @Test
    void rejectRequest_AlreadyRejected_ThrowsException() {
        // Arrange
        orderRequest.setDecision(OrderRequest.Status.REJECTED);
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.rejectRequest(1L);
        });
        
        assertEquals("Request already processed", exception.getMessage());
        verify(orderRequestRepo, never()).save(any());
    }

    @Test
    void getAllRequests_ReturnsListOfOrderRequests() {
        // Arrange
        OrderRequest orderRequest2 = new OrderRequest();
        orderRequest2.setReqId(2L);
        
        List<OrderRequest> expectedRequests = Arrays.asList(orderRequest, orderRequest2);
        when(orderRequestRepo.findAll()).thenReturn(expectedRequests);
        
        // Act
        List<OrderRequest> result = orderRequestService.getAllRequests();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRequestRepo, times(1)).findAll();
    }

    @Test
    void getAllRequests_EmptyRepository_ReturnsEmptyList() {
        // Arrange
        when(orderRequestRepo.findAll()).thenReturn(Arrays.asList());
        
        // Act
        List<OrderRequest> result = orderRequestService.getAllRequests();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRequestRepo, times(1)).findAll();
    }

    @Test
    void listByBusinessOwner_ValidId_ReturnsOrderRequests() {
        // Arrange
        OrderRequest orderRequest2 = new OrderRequest();
        orderRequest2.setReqId(2L);
        
        List<OrderRequest> expectedRequests = Arrays.asList(orderRequest, orderRequest2);
        when(orderRequestRepo.findByBusinessOwnerId(1L)).thenReturn(expectedRequests);
        
        // Act
        List<OrderRequest> result = orderRequestService.listByBusinessOwner(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRequestRepo, times(1)).findByBusinessOwnerId(1L);
    }

    @Test
    void listByBusinessOwner_NoRequests_ReturnsEmptyList() {
        // Arrange
        when(orderRequestRepo.findByBusinessOwnerId(1L)).thenReturn(Arrays.asList());
        
        // Act
        List<OrderRequest> result = orderRequestService.listByBusinessOwner(1L);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRequestRepo, times(1)).findByBusinessOwnerId(1L);
    }

    @Test
    void getRequestByIdAndBusinessOwner_ValidIds_ReturnsOrderRequest() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act
        OrderRequest result = orderRequestService.getRequestByIdAndBusinessOwner(1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getReqId());
        verify(orderRequestRepo, times(1)).findById(1L);
    }

    @Test
    void getRequestByIdAndBusinessOwner_RequestNotFound_ThrowsException() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.getRequestByIdAndBusinessOwner(1L, 1L);
        });
        
        assertEquals("Request not found or access denied", exception.getMessage());
        verify(orderRequestRepo, times(1)).findById(1L);
    }

    @Test
    void getRequestByIdAndBusinessOwner_DifferentBusinessOwner_ThrowsException() {
        // Arrange
        BusinessOwner differentOwner = new BusinessOwner();
        differentOwner.setId(2L);
        orderRequest.setBusinessOwner(differentOwner);
        
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.getRequestByIdAndBusinessOwner(1L, 1L);
        });
        
        assertEquals("Request not found or access denied", exception.getMessage());
        verify(orderRequestRepo, times(1)).findById(1L);
    }

    @Test
    void getRequestById_ValidId_ReturnsOrderRequest() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        
        // Act
        OrderRequest result = orderRequestService.getRequestById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getReqId());
        verify(orderRequestRepo, times(1)).findById(1L);
    }

    @Test
    void getRequestById_RequestNotFound_ThrowsException() {
        // Arrange
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.getRequestById(1L);
        });
        
        assertEquals("Request not found", exception.getMessage());
        verify(orderRequestRepo, times(1)).findById(1L);
    }

    @Test
    void submitRequest_ExtremeCoordinates_CalculatesDistanceCorrectly() {
        // Test with coordinates at the poles and equator
        orderCreateRequest.setFromLatitude(90.0); // North Pole
        orderCreateRequest.setFromLongitude(0.0);
        orderCreateRequest.setToLatitude(-90.0); // South Pole
        orderCreateRequest.setToLongitude(0.0);
        
        when(orderRequestRepo.save(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest savedRequest = invocation.getArgument(0);
            savedRequest.setReqId(1L);
            return savedRequest;
        });
        
        OrderRequest result = orderRequestService.submitRequest(businessOwner, orderCreateRequest);
        
        // Distance between poles should be approximately 20000 km (half circumference)
        assertTrue(result.getDeliveryDistance() > 19000 && result.getDeliveryDistance() < 21000);
    }

    @Test
    void submitRequest_SameCoordinates_CalculatesZeroDistance() {
        // Test with identical coordinates
        orderCreateRequest.setFromLatitude(40.7128);
        orderCreateRequest.setFromLongitude(-74.0060);
        orderCreateRequest.setToLatitude(40.7128);
        orderCreateRequest.setToLongitude(-74.0060);
        
        when(orderRequestRepo.save(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest savedRequest = invocation.getArgument(0);
            savedRequest.setReqId(1L);
            return savedRequest;
        });
        
        OrderRequest result = orderRequestService.submitRequest(businessOwner, orderCreateRequest);
        
        assertEquals(0.0, result.getDeliveryDistance(), 0.001);
    }

    // Additional test for warehouse assignment
    @Test
    void approveRequest_WarehouseAssignment_ProperlySetsWarehouse() {
        // Arrange
        Warehouse assignedWarehouse = new Warehouse();
        assignedWarehouse.setWarehouseId(2L);
        assignedWarehouse.setName("Assigned Warehouse");
        
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(warehouseService.assignWarehouse(any(Order.class))).thenReturn(assignedWarehouse);
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setOrderId(1L);
            savedOrder.setAssignedWarehouse(assignedWarehouse);
            return savedOrder;
        });
        
        // Act
        OrderResponse result = orderRequestService.approveRequest(1L);
        
        // Assert
        assertNotNull(result);
        verify(warehouseService, times(1)).assignWarehouse(any(Order.class));
    }
}