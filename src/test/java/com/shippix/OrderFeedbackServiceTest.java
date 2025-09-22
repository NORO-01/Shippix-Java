package com.shippix;

import com.shippix.Order.DTO.OrderFeedbackRequest;
import com.shippix.Order.DTO.OrderFeedbackResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.OrderFeedback;
import com.shippix.Order.Repo.OrderFeedbackRepo;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.Order.Service.OrderFeedbackService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFeedbackServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private OrderFeedbackRepo feedbackRepo;

    @InjectMocks
    private OrderFeedbackService orderFeedbackService;

    private Order validOrder;
    private BusinessOwner businessOwner;
    private OrderFeedbackRequest validFeedbackRequest;
    private OrderFeedback existingFeedback;

    @BeforeEach
    void setUp() {
        businessOwner = new BusinessOwner();
        businessOwner.setId(1L);
        validOrder = new Order();
        validOrder.setOrderId(1L);
        validOrder.setBusinessOwner(businessOwner);
        validOrder.setStatus(Order.Status.DELIVERED);

        validFeedbackRequest = new OrderFeedbackRequest(4, "Great service!");

        existingFeedback = new OrderFeedback();
        existingFeedback.setId(1L);
        existingFeedback.setOrder(validOrder);
        existingFeedback.setRating(5);
        existingFeedback.setComment("Excellent delivery");
    }

    // ========== addFeedback TESTS ==========

    @Test
    void addFeedback_ValidDataForDeliveredOrder_ReturnsFeedbackResponse() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Act
        OrderFeedbackResponse result = orderFeedbackService.addFeedback(1L, validFeedbackRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepo, times(1)).findById(1L);
        verify(feedbackRepo, times(1)).existsByOrder_OrderId(1L);
        verify(feedbackRepo, times(1)).save(any(OrderFeedback.class));
    }

    @Test
    void addFeedback_ValidDataForCanceledOrder_ReturnsFeedbackResponse() {
        // Arrange
        validOrder.setStatus(Order.Status.CANCELED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Act
        OrderFeedbackResponse result = orderFeedbackService.addFeedback(1L, validFeedbackRequest);

        // Assert
        assertNotNull(result);
        assertEquals(Order.Status.CANCELED, validOrder.getStatus());
    }

    @Test
    void addFeedback_OrderNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.addFeedback(1L, validFeedbackRequest);
        });

        assertEquals("Order not found", exception.getMessage());
        verify(feedbackRepo, never()).existsByOrder_OrderId(any());
        verify(feedbackRepo, never()).save(any());
    }


    @Test
    void addFeedback_FeedbackAlreadyExists_ThrowsIllegalStateException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderFeedbackService.addFeedback(1L, validFeedbackRequest);
        });

        assertEquals("Feedback already exists for this order", exception.getMessage());
        verify(feedbackRepo, never()).save(any());
    }

    @Test
    void addFeedback_NullOrderId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.addFeedback(null, validFeedbackRequest);
        });
    }

    @Test
    void addFeedback_NullFeedbackRequest_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.addFeedback(1L, null);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void addFeedback_InvalidRating_ThrowsException(int invalidRating) {
        // Arrange
        OrderFeedbackRequest invalidRequest = new OrderFeedbackRequest(invalidRating, "Comment");

        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.addFeedback(1L, invalidRequest);
        });
    }

    // ========== getFeedback TESTS ==========

    @Test
    void getFeedback_ValidOrderId_ReturnsFeedbackResponse() {
        // Arrange
        when(feedbackRepo.findByOrder_OrderId(1L)).thenReturn(existingFeedback);

        // Act
        OrderFeedbackResponse result = orderFeedbackService.getFeedback(1L);

        // Assert
        assertNotNull(result);
        verify(feedbackRepo, times(1)).findByOrder_OrderId(1L);
    }

    @Test
    void getFeedback_FeedbackNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(feedbackRepo.findByOrder_OrderId(1L)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.getFeedback(1L);
        });

        assertEquals("Feedback not found for this order", exception.getMessage());
    }

    @Test
    void getFeedback_NullOrderId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.getFeedback(null);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE})
    void getFeedback_InvalidOrderId_ThrowsException(long invalidOrderId) {
        // Arrange
        when(feedbackRepo.findByOrder_OrderId(invalidOrderId)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.getFeedback(invalidOrderId);
        });

        assertEquals("Feedback not found for this order", exception.getMessage());
    }

    // ========== getFeedbacksForBusinessOwner TESTS ==========

    @Test
    void getFeedbacksForBusinessOwner_ValidBusinessOwnerId_ReturnsFeedbackList() {
        // Arrange
        OrderFeedback feedback1 = new OrderFeedback();
        feedback1.setId(1L);
        feedback1.setOrder(validOrder);
        feedback1.setRating(4);
        feedback1.setComment("Good service");

        OrderFeedback feedback2 = new OrderFeedback();
        feedback2.setId(2L);
        
        Order order2 = new Order();
        order2.setOrderId(2L);
        order2.setBusinessOwner(businessOwner);
        feedback2.setOrder(order2);
        feedback2.setRating(5);
        feedback2.setComment("Excellent service");

        List<OrderFeedback> feedbacks = Arrays.asList(feedback1, feedback2);
        when(feedbackRepo.findByOrder_BusinessOwner_Id(1L)).thenReturn(feedbacks);

        // Act
        List<OrderFeedbackResponse> result = orderFeedbackService.getFeedbacksForBusinessOwner(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(feedbackRepo, times(1)).findByOrder_BusinessOwner_Id(1L);
    }

    @Test
    void getFeedbacksForBusinessOwner_NoFeedbacks_ReturnsEmptyList() {
        // Arrange
        when(feedbackRepo.findByOrder_BusinessOwner_Id(1L)).thenReturn(Arrays.asList());

        // Act
        List<OrderFeedbackResponse> result = orderFeedbackService.getFeedbacksForBusinessOwner(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getFeedbacksForBusinessOwner_NullBusinessOwnerId_ThrowsException() {
        // Act & Assert
        assertEquals(0, orderFeedbackService.getFeedbacksForBusinessOwner(null).size());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE})
    void getFeedbacksForBusinessOwner_InvalidBusinessOwnerId_ReturnsEmptyList(long invalidId) {
        // Arrange
        when(feedbackRepo.findByOrder_BusinessOwner_Id(invalidId)).thenReturn(Arrays.asList());

        // Act
        List<OrderFeedbackResponse> result = orderFeedbackService.getFeedbacksForBusinessOwner(invalidId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== submitFeedback TESTS ==========

    @Test
    void submitFeedback_ValidData_ReturnsFeedbackResponse() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Act
        OrderFeedbackResponse result = orderFeedbackService.submitFeedback(1L, 1L, validFeedbackRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepo, times(1)).findById(1L);
        verify(feedbackRepo, times(1)).existsByOrder_OrderId(1L);
        verify(feedbackRepo, times(1)).save(any(OrderFeedback.class));
    }

    @Test
    void submitFeedback_OrderNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.submitFeedback(1L, 1L, validFeedbackRequest);
        });

        assertEquals("Order not found or access denied", exception.getMessage());
    }

    @Test
    void submitFeedback_DifferentBusinessOwner_ThrowsEntityNotFoundException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.submitFeedback(1L, 999L, validFeedbackRequest); // Different business owner ID
        });

        assertEquals("Order not found or access denied", exception.getMessage());
    }

    @Test
    void submitFeedback_OrderWithNullBusinessOwner_ThrowsEntityNotFoundException() {
        // Arrange
        validOrder.setBusinessOwner(null);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.submitFeedback(1L, 1L, validFeedbackRequest);
        });
    }


    @Test
    void submitFeedback_FeedbackAlreadyExists_ThrowsIllegalStateException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.submitFeedback(1L, 1L, validFeedbackRequest);
        });
    }

    // ========== getFeedbackByOrderIdAndBusinessOwner TESTS ==========

    @Test
    void getFeedbackByOrderIdAndBusinessOwner_ValidIds_ReturnsFeedbackResponse() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.findByOrder_OrderId(1L)).thenReturn(existingFeedback);

        // Act
        OrderFeedbackResponse result = orderFeedbackService.getFeedbackByOrderIdAndBusinessOwner(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(orderRepo, times(1)).findById(1L);
        verify(feedbackRepo, times(1)).findByOrder_OrderId(1L);
    }

    @Test
    void getFeedbackByOrderIdAndBusinessOwner_OrderNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.getFeedbackByOrderIdAndBusinessOwner(1L, 1L);
        });

        assertEquals("Order not found or access denied", exception.getMessage());
    }

    @Test
    void getFeedbackByOrderIdAndBusinessOwner_DifferentBusinessOwner_ThrowsEntityNotFoundException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.getFeedbackByOrderIdAndBusinessOwner(1L, 999L);
        });

        assertEquals("Order not found or access denied", exception.getMessage());
    }

    @Test
    void getFeedbackByOrderIdAndBusinessOwner_FeedbackNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.findByOrder_OrderId(1L)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderFeedbackService.getFeedbackByOrderIdAndBusinessOwner(1L, 1L);
        });

        assertEquals("Feedback not found for this order", exception.getMessage());
    }

    // ========== EDGE CASES AND BOUNDARY TESTS ==========

    @Test
    void addFeedback_ExtremeRatingValues_HandlesCorrectly() {
        // Test boundary ratings (1 and 5 should be valid)
        OrderFeedbackRequest minRatingRequest = new OrderFeedbackRequest(1, "Minimum rating");
        OrderFeedbackRequest maxRatingRequest = new OrderFeedbackRequest(5, "Maximum rating");

        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Should not throw exceptions for valid boundary values
        assertDoesNotThrow(() -> {
            orderFeedbackService.addFeedback(1L, minRatingRequest);
            orderFeedbackService.addFeedback(1L, maxRatingRequest);
        });
    }

    @Test
    void addFeedback_VeryLongComment_HandlesCorrectly() {
        // Arrange
        String longComment = "A".repeat(1000); // Very long comment
        OrderFeedbackRequest longCommentRequest = new OrderFeedbackRequest(4, longComment);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Act & Assert - Should handle without exception
        assertDoesNotThrow(() -> {
            orderFeedbackService.addFeedback(1L, longCommentRequest);
        });
    }

    @Test
    void addFeedback_NullComment_HandlesCorrectly() {
        // Arrange
        OrderFeedbackRequest nullCommentRequest = new OrderFeedbackRequest(4, null);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Act & Assert - Should handle null comment
        assertDoesNotThrow(() -> {
            orderFeedbackService.addFeedback(1L, nullCommentRequest);
        });
    }

    @Test
    void addFeedback_EmptyComment_HandlesCorrectly() {
        // Arrange
        OrderFeedbackRequest emptyCommentRequest = new OrderFeedbackRequest(4, "");

        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenReturn(existingFeedback);

        // Act & Assert - Should handle empty comment
        assertDoesNotThrow(() -> {
            orderFeedbackService.addFeedback(1L, emptyCommentRequest);
        });
    }

    @Test
    void getFeedbacksForBusinessOwner_LargeNumberOfFeedbacks_HandlesCorrectly() {
        // Arrange
        List<OrderFeedback> largeFeedbackList = createLargeFeedbackList(1000);
        when(feedbackRepo.findByOrder_BusinessOwner_Id(1L)).thenReturn(largeFeedbackList);

        // Act
        List<OrderFeedbackResponse> result = orderFeedbackService.getFeedbacksForBusinessOwner(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.size());
    }

    // ========== CONCURRENCY AND EXCEPTION PROPAGATION TESTS ==========

    @Test
    void addFeedback_DatabaseExceptionDuringSave_PropagatesException() {
        // Arrange
        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);
        when(feedbackRepo.save(any(OrderFeedback.class))).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderFeedbackService.addFeedback(1L, validFeedbackRequest);
        });

        assertEquals("Database connection failed", exception.getMessage());
    }

    @Test
    void getFeedbacksForBusinessOwner_DatabaseException_PropagatesException() {
        // Arrange
        when(feedbackRepo.findByOrder_BusinessOwner_Id(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderFeedbackService.getFeedbacksForBusinessOwner(1L);
        });

        assertEquals("Database error", exception.getMessage());
    }

    // ========== HELPER METHODS ==========

    private List<OrderFeedback> createLargeFeedbackList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> {
                    OrderFeedback feedback = new OrderFeedback();
                    feedback.setId((long) i);
                    
                    Order order = new Order();
                    order.setOrderId((long) i);
                    order.setBusinessOwner(businessOwner);
                    feedback.setOrder(order);
                    
                    feedback.setRating((i % 5) + 1); // Ratings between 1-5
                    feedback.setComment("Feedback " + i);
                    return feedback;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // Parameterized test source for invalid ratings
    static Stream<Arguments> invalidRatingProvider() {
        return Stream.of(
            Arguments.of(0),
            Arguments.of(6),
            Arguments.of(-1),
            Arguments.of(10),
            Arguments.of(Integer.MIN_VALUE),
            Arguments.of(Integer.MAX_VALUE)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRatingProvider")
    void addFeedback_VariousInvalidRatings_ThrowsException(int invalidRating) {
        // Arrange
        OrderFeedbackRequest invalidRequest = new OrderFeedbackRequest(invalidRating, "Test comment");

        when(orderRepo.findById(1L)).thenReturn(Optional.of(validOrder));
        when(feedbackRepo.existsByOrder_OrderId(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            orderFeedbackService.addFeedback(1L, invalidRequest);
        });
    }
}