package com.shippix;

import com.shippix.Payment.dto.PaymentRequest;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentMethod;
import com.shippix.Payment.enums.PaymentStatus;
import com.shippix.Payment.model.Payment;
import com.shippix.Payment.repo.PaymentRepo;
import com.shippix.Payment.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepo paymentRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest request;
    private Payment payment;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest();
        request.setOrderId("order-123");
        request.setBasePrice(BigDecimal.valueOf(100));
        request.setWeightFactor(BigDecimal.valueOf(2));
        request.setDistanceFactor(BigDecimal.valueOf(5));
        request.setMethod(PaymentMethod.VISA_CARD);

        payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setBasePrice(request.getBasePrice());
        payment.setWeightFactor(request.getWeightFactor());
        payment.setDistanceFactor(request.getDistanceFactor());
        payment.setCalculatedPrice(BigDecimal.valueOf(110));
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);

        response = new PaymentResponse();
        response.setOrderId("order-123");
        response.setCalculatedPrice(BigDecimal.valueOf(110));
        response.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void createPayment_ShouldSaveAndReturnResponse() {
        when(paymentRepo.findByOrderId("order-123")).thenReturn(Optional.empty());
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertNotNull(result);
        assertEquals("order-123", result.getOrderId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(BigDecimal.valueOf(110), result.getCalculatedPrice());

        verify(paymentRepo).findByOrderId("order-123");
        verify(paymentRepo).save(any(Payment.class));
        verify(modelMapper).map(payment, PaymentResponse.class);
    }

    @Test
    void createPayment_WhenOrderAlreadyPaid_ShouldThrowException() {
        when(paymentRepo.findByOrderId("order-123")).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(request));

        verify(paymentRepo).findByOrderId("order-123");
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void simulatePayment_WhenSuccess_ShouldUpdateStatusAndReturnResponse() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepo.findByOrderId("order-123")).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = paymentService.simulatePayment("order-123", true);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getReferenceCode());
        assertEquals("order-123", result.getOrderId());

        verify(paymentRepo).findByOrderId("order-123");
        verify(paymentRepo).save(payment);
    }

    @Test
    void simulatePayment_WhenFailed_ShouldUpdateStatusToFailed() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepo.findByOrderId("order-123")).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = paymentService.simulatePayment("order-123", false);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertNull(payment.getReferenceCode());
        assertEquals("order-123", result.getOrderId());

        verify(paymentRepo).findByOrderId("order-123");
        verify(paymentRepo).save(payment);
    }

    @Test
    void simulatePayment_WhenPaymentNotFound_ShouldThrowException() {
        when(paymentRepo.findByOrderId("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.simulatePayment("invalid", true));

        verify(paymentRepo).findByOrderId("invalid");
    }

    @Test
    void getPayment_ShouldReturnMappedResponse() {
        when(paymentRepo.findByOrderId("order-123")).thenReturn(Optional.of(payment));
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = paymentService.getPayment("order-123");

        assertEquals("order-123", result.getOrderId());
        verify(paymentRepo).findByOrderId("order-123");
        verify(modelMapper).map(payment, PaymentResponse.class);
    }

    @Test
    void getPayment_WhenNotFound_ShouldThrowException() {
        when(paymentRepo.findByOrderId("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPayment("invalid"));
    }

    @Test
    void getAllPayments_ShouldReturnListOfResponses() {
        when(paymentRepo.findAll()).thenReturn(List.of(payment));
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        List<PaymentResponse> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
        assertEquals("order-123", result.get(0).getOrderId());

        verify(paymentRepo).findAll();
        verify(modelMapper).map(payment, PaymentResponse.class);
    }

    @Test
    void getAllPayments_WhenEmpty_ShouldReturnEmptyList() {
        when(paymentRepo.findAll()).thenReturn(Collections.emptyList());

        List<PaymentResponse> result = paymentService.getAllPayments();

        assertTrue(result.isEmpty());
        verify(paymentRepo).findAll();
        verifyNoInteractions(modelMapper);
    }
}
