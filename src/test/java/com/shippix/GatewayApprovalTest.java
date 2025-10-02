package com.shippix;

import com.shippix.Order.Model.OrderRequest;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentStatus;
import com.shippix.Payment.model.Payment;
import com.shippix.Payment.repo.PaymentRepo;
import com.shippix.Payment.service.GatewayApproval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayApprovalTest {

    @Mock
    private PaymentRepo paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GatewayApproval gatewayApproval;

    private OrderRequest orderRequest;
    private Payment payment;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {

        orderRequest = new OrderRequest();
        orderRequest.setReqId(1L);

        payment = new Payment();
        payment.setOrderRequest(orderRequest);
        payment.setCode("PAY-123");
        payment.setStatus(PaymentStatus.PENDING);

        response = new PaymentResponse();
        response.setOrderRequestId(1L);
        response.setStatus(PaymentStatus.SUCCESS);
    }

    @Test
    void validatePayment_WhenCodeMatches_ShouldSetSuccess() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = gatewayApproval.validatePayment(1L, "PAY-123");

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());

        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(payment);
        verify(modelMapper).map(payment, PaymentResponse.class);
    }

    @Test
    void validatePayment_WhenCodeDoesNotMatch_ShouldSetFailed() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        response.setStatus(PaymentStatus.FAILED);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = gatewayApproval.validatePayment(1L, "WRONG-CODE");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(PaymentStatus.FAILED, result.getStatus());

        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(payment);
        verify(modelMapper).map(payment, PaymentResponse.class);
    }

    @Test
    void validatePayment_WhenPaymentNotFound_ShouldThrowException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> gatewayApproval.validatePayment(99L, "ANY"));

        verify(paymentRepository).findById(99L);
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(modelMapper);
    }

    @Test
    void validatePayment_WhenPaymentAlreadySuccess_ShouldRemainSuccess() {
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = gatewayApproval.validatePayment(1L, "PAY-123");

        assertEquals(PaymentStatus.SUCCESS, result.getStatus(), 
            "Already successful payment should remain success");
    }

    @Test
    void validatePayment_WhenPaymentAlreadyFailed_ShouldBeOverwrittenOnMatch() {
        payment.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentResponse.class)).thenReturn(response);

        PaymentResponse result = gatewayApproval.validatePayment(1L, "PAY-123");

        assertEquals(PaymentStatus.SUCCESS, result.getStatus(), 
            "Failed payment should be corrected to success if code matches");
    }

    @Test
    void validatePayment_WhenPaymentHasNullCode_ShouldAlwaysFail() {
        payment.setCode(null);
        payment.setStatus(PaymentStatus.FAILED);
        assertThrows(RuntimeException.class, () -> gatewayApproval.validatePayment(1L, "ANY"));
    }

}
