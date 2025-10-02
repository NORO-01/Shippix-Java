package com.shippix;

import com.shippix.Order.Model.OrderRequest;
import com.shippix.Order.Repo.OrderRequestRepo;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentMethod;
import com.shippix.Payment.enums.PaymentStatus;
import com.shippix.Payment.model.Payment;
import com.shippix.Payment.repo.PaymentRepo;
import com.shippix.Payment.service.CreatePayment;
import com.shippix.Payment.service.NotifyPaymentService;
import com.shippix.Payment.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepo paymentRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PricingService pricingService;

    @Mock
    private OrderRequestRepo orderRequestRepo;

    @Mock
    private NotifyPaymentService notifyPaymentService;

    @InjectMocks
    private CreatePayment paymentService;

    private OrderRequest orderRequest;
    private Payment payment;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setReqId(1L);

        payment = new Payment();
        payment.setPaymentId(10L);
        payment.setOrderRequest(orderRequest);
        payment.setAmount(150.0);
        payment.setMethod(PaymentMethod.FAWRY);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCode("PAY-123");

        response = new PaymentResponse();
        response.setOrderRequestId(1L);
        response.setAmount(payment.getAmount());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
    }

    @Test
    void createPayment_ShouldSaveAndReturnResponse() {
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(paymentRepo.findByOrderRequest_ReqId(1L)).thenReturn(Optional.empty());
        when(pricingService.calculatePrice(orderRequest)).thenReturn(150.0);
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(1L, PaymentMethod.FAWRY);

        assertNotNull(result);
        assertEquals(1L, result.getOrderRequestId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(150.0, result.getAmount());

        verify(orderRequestRepo).findById(1L);
        verify(paymentRepo).findByOrderRequest_ReqId(1L);
        verify(pricingService).calculatePrice(orderRequest);
        verify(paymentRepo).save(any(Payment.class));
        verify(modelMapper).map(payment, PaymentResponse.class);
        verify(notifyPaymentService).sendPaymentCode(payment);
    }

    @Test
    void createPayment_WhenRequestAlreadyPaid_ShouldThrowException() {
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(paymentRepo.findByOrderRequest_ReqId(1L)).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(1L, PaymentMethod.FAWRY));

        verify(paymentRepo).findByOrderRequest_ReqId(1L);
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void createPayment_WhenOrderNotFound_ShouldThrowException() {
        when(orderRequestRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(99L, PaymentMethod.FAWRY));

        verify(orderRequestRepo).findById(99L);
        verifyNoInteractions(paymentRepo, pricingService, modelMapper);
    }

    @Test
    void createPayment_WhenMethodIsFawry_ShouldSendNotification() {
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(paymentRepo.findByOrderRequest_ReqId(1L)).thenReturn(Optional.empty());
        when(pricingService.calculatePrice(orderRequest)).thenReturn(200.0);
        payment.setMethod(PaymentMethod.FAWRY);
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(1L, PaymentMethod.FAWRY);

        assertEquals(1L, result.getOrderRequestId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());

        verify(notifyPaymentService).sendPaymentCode(payment);
    }

    @Test
    void createPayment_ShouldGenerateUniqueCodeEachTime() {
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(paymentRepo.findByOrderRequest_ReqId(1L)).thenReturn(Optional.empty());
        when(pricingService.calculatePrice(orderRequest)).thenReturn(100.0);
        when(paymentRepo.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // dynamically map instead of fixed response
        when(modelMapper.map(any(Payment.class), eq(PaymentResponse.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            PaymentResponse resp = new PaymentResponse();
            resp.setOrderRequestId(payment.getOrderRequest().getReqId());
            resp.setStatus(payment.getStatus());
            resp.setAmount(payment.getAmount());
            resp.setCode(payment.getCode()); // important!
            return resp;
        });

        PaymentResponse result1 = paymentService.createPayment(1L, PaymentMethod.FAWRY);
        PaymentResponse result2 = paymentService.createPayment(1L, PaymentMethod.FAWRY);

        System.out.println("Code1 = " + result1.getCode());
        System.out.println("Code2 = " + result2.getCode());

        assertNotNull(result1.getCode());
        assertNotNull(result2.getCode());
        assertNotEquals(result1.getCode(), result2.getCode(), "Each call should generate a unique code");
    }


    @Test
    void createPayment_WhenRepoSaveFails_ShouldPropagateException() {
        when(orderRequestRepo.findById(1L)).thenReturn(Optional.of(orderRequest));
        when(paymentRepo.findByOrderRequest_ReqId(1L)).thenReturn(Optional.empty());
        when(pricingService.calculatePrice(orderRequest)).thenReturn(120.0);
        when(paymentRepo.save(any(Payment.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> 
            paymentService.createPayment(1L, PaymentMethod.FAWRY));
    }

}
