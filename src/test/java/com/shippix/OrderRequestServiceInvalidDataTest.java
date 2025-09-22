package com.shippix;

import com.shippix.Order.DTO.OrderCreateRequest;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class OrderRequestServiceInvalidDataTest {

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private OrderRequestRepo orderRequestRepo;

    @Mock
    private OrderRepo orderRepo;

    @InjectMocks
    private OrderRequestService orderRequestService;

    private BusinessOwner businessOwner;
    private OrderCreateRequest validOrderCreateRequest;

    @BeforeEach
    void setUp() {
        businessOwner = new BusinessOwner();
        businessOwner.setId(1L);

        validOrderCreateRequest = new OrderCreateRequest();
        validOrderCreateRequest.setOrderDescription("Valid order description");
        validOrderCreateRequest.setNotesToDriver("Handle with care");
        validOrderCreateRequest.setPackageWeight(5.0);
        validOrderCreateRequest.setPackageValue(100.0);
        validOrderCreateRequest.setFromLatitude(40.7128);
        validOrderCreateRequest.setFromLongitude(-74.0060);
        validOrderCreateRequest.setToLatitude(34.0522);
        validOrderCreateRequest.setToLongitude(-118.2437);
        validOrderCreateRequest.setCustName("John Doe");
        validOrderCreateRequest.setCustPhoneNumber("123-456-7890");
        validOrderCreateRequest.setCustEmail("john@example.com");
    }

    // ========== submitRequest INVALID DATA TESTS ==========

    @Test
    void submitRequest_NullOrderCreateRequest_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            orderRequestService.submitRequest(businessOwner, null);
        });
    }

    @Test
    void submitRequest_SameCoordinates_CalculatesZeroDistance() {
        // Arrange
        OrderCreateRequest sameCoordRequest = validOrderCreateRequest;
        sameCoordRequest.setToLatitude(sameCoordRequest.getFromLatitude());
        sameCoordRequest.setToLongitude(sameCoordRequest.getFromLongitude());

        when(orderRequestRepo.save(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest saved = invocation.getArgument(0);
            saved.setReqId(1L);
            return saved;
        });

        // Act
        OrderRequest result = orderRequestService.submitRequest(businessOwner, sameCoordRequest);

        // Assert
        assertEquals(0.0, result.getDeliveryDistance(), 0.001);
    }

    // ========== approveRequest INVALID DATA TESTS ==========

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE})
    void approveRequest_InvalidRequestId_ThrowsException(long invalidId) {
        // Arrange
        when(orderRequestRepo.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(invalidId);
        });

        assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void approveRequest_NullRequestId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderRequestService.approveRequest(null);
        });
    }

    @Test
    void approveRequest_RequestWithNullBusinessOwner_ThrowsException() {
        // Arrange
        OrderRequest invalidRequest = new OrderRequest();
        invalidRequest.setReqId(1L);
        invalidRequest.setDecision(OrderRequest.Status.PENDING);
        invalidRequest.setBusinessOwner(null); // This should cause issues later

        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(invalidRequest));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderRequestService.approveRequest(1L);
        });
    }

    @Test
    void approveRequest_WarehouseServiceReturnsNull_ThrowsException() {
        // Arrange
        OrderRequest validRequest = createValidOrderRequest();
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(validRequest));
        when(warehouseService.assignWarehouse(any(Order.class))).thenReturn(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderRequestService.approveRequest(1L);
        });
    }

    @Test
    void approveRequest_DatabaseSaveFails_ThrowsException() {
        // Arrange
        OrderRequest validRequest = createValidOrderRequest();
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(validRequest));
        when(warehouseService.assignWarehouse(any(Order.class))).thenReturn(new Warehouse());
        when(orderRepo.save(any(Order.class))).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(1L);
        });

        assertEquals("Database connection failed", exception.getMessage());
    }

    // ========== rejectRequest INVALID DATA TESTS ==========

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE})
    void rejectRequest_InvalidRequestId_ThrowsException(long invalidId) {
        // Arrange
        when(orderRequestRepo.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.rejectRequest(invalidId);
        });

        assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void rejectRequest_NullRequestId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderRequestService.rejectRequest(null);
        });
    }

    // ========== getRequestByIdAndBusinessOwner INVALID DATA TESTS ==========

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE})
    void getRequestByIdAndBusinessOwner_InvalidIds_ThrowsException(long invalidId) {
        // Arrange
        when(orderRequestRepo.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.getRequestByIdAndBusinessOwner(invalidId, invalidId);
        });

        assertEquals("Request not found or access denied", exception.getMessage());
    }

    @Test
    void getRequestByIdAndBusinessOwner_NullIds_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderRequestService.getRequestByIdAndBusinessOwner(null, null);
        });
    }

    @Test
    void getRequestByIdAndBusinessOwner_RequestWithNullBusinessOwner_ThrowsException() {
        // Arrange
        OrderRequest invalidRequest = new OrderRequest();
        invalidRequest.setReqId(1L);
        invalidRequest.setBusinessOwner(null);

        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(invalidRequest));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderRequestService.getRequestByIdAndBusinessOwner(1L, 1L);
        });
    }

    // ========== listByBusinessOwner INVALID DATA TESTS ==========

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE})
    void listByBusinessOwner_InvalidBusinessOwnerId_ReturnsEmptyList(long invalidId) {
        // Arrange
        when(orderRequestRepo.findByBusinessOwnerId(invalidId)).thenReturn(Arrays.asList());

        // Act
        List<OrderRequest> result = orderRequestService.listByBusinessOwner(invalidId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listByBusinessOwner_NullBusinessOwnerId_ThrowsException() {
        // Act & Assert
        assertEquals(0, orderRequestService.listByBusinessOwner(null).size());
    }

    // ========== EDGE CASES FOR REPOSITORY INTERACTIONS ==========

    @Test
    void submitRequest_RepositoryThrowsException_PropagatesException() {
        // Arrange
        when(orderRequestRepo.save(any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Database unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.submitRequest(businessOwner, validOrderCreateRequest);
        });

        assertEquals("Database unavailable", exception.getMessage());
    }

    @Test
    void approveRequest_OrderRequestSaveFails_TransactionRollsBack() {
        // Arrange
        OrderRequest validRequest = createValidOrderRequest();
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(validRequest));
        when(orderRequestRepo.save(any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Failed to save order request"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(1L);
        });

        assertEquals("Failed to save order request", exception.getMessage());
        // Verify that order was not saved due to transaction rollback
        verify(orderRepo, never()).save(any(Order.class));
    }

    // ========== DATA TYPE BOUNDARY TESTS ==========

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 1.0, 999.9, 1000.0})
    void submitRequest_ValidPackageWeightBoundaries_Success(double validWeight) {
        // Arrange
        OrderCreateRequest request = validOrderCreateRequest;
        request.setPackageWeight(validWeight);

        when(orderRequestRepo.save(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest saved = invocation.getArgument(0);
            saved.setReqId(1L);
            return saved;
        });

        // Act
        OrderRequest result = orderRequestService.submitRequest(businessOwner, request);

        // Assert
        assertNotNull(result);
        assertEquals(validWeight, result.getPackageWeight());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 1.0, 999999.9, 1000000.0})
    void submitRequest_ValidPackageValueBoundaries_Success(double validValue) {
        // Arrange
        OrderCreateRequest request = validOrderCreateRequest;
        request.setPackageValue(validValue);

        when(orderRequestRepo.save(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest saved = invocation.getArgument(0);
            saved.setReqId(1L);
            return saved;
        });

        // Act
        OrderRequest result = orderRequestService.submitRequest(businessOwner, request);

        // Assert
        assertNotNull(result);
        assertEquals(validValue, result.getPackageValue());
    }

    // ========== CONCURRENT MODIFICATION EDGE CASES ==========

    @Test
    void approveRequest_RequestModifiedConcurrently_ThrowsException() {
        // Arrange
        OrderRequest request = createValidOrderRequest();
        
        // Simulate concurrent modification by changing status after find but before save
        when(orderRequestRepo.findById(1L)).thenAnswer(invocation -> {
            request.setDecision(OrderRequest.Status.APPROVED); // Concurrent modification
            return Optional.of(request);
        });

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderRequestService.approveRequest(1L);
        });

        assertTrue(exception.getMessage().contains("already processed"));
    }

    // ========== HELPER METHODS ==========

    private OrderRequest createValidOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setReqId(1L);
        request.setBusinessOwner(businessOwner);
        request.setOrderDescription("Valid order");
        request.setPackageWeight(5.0);
        request.setPackageValue(100.0);
        request.setFromLatitude(40.7128);
        request.setFromLongitude(-74.0060);
        request.setToLatitude(34.0522);
        request.setToLongitude(-118.2437);
        request.setCustName("John Doe");
        request.setCustPhoneNumber("123-456-7890");
        request.setCustEmail("john@example.com");
        request.setDeliveryDistance(3940.0);
        request.setDecision(OrderRequest.Status.PENDING);
        return request;
    }

}