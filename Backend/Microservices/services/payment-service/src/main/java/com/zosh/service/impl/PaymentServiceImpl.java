package com.zosh.service.impl;

import com.razorpay.RazorpayException;
import com.zosh.client.UserClient;
import com.zosh.event.PaymentEventProducer;
import com.zosh.payload.dto.PaymentDTO;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.PaymentInitiateRequest;
import com.zosh.payload.response.PaymentInitiateResponse;
import com.zosh.payload.request.PaymentVerifyRequest;
import com.zosh.enums.PaymentGateway;
import com.zosh.enums.PaymentStatus;
import com.zosh.payload.response.PaymentLinkResponse;
import com.zosh.service.gateway.RazorpayService;
import org.json.JSONObject;
import com.zosh.mapper.PaymentMapper;
import com.zosh.model.Payment;
import com.zosh.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.zosh.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentEventProducer paymentEventProducer;
    private final UserClient userClient;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) throws RazorpayException {

        paymentRepository.findByBookingId(request.getBookingId())
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        throw new RuntimeException(
                                "Payment already completed for this booking");
                    }
                });

        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .provider(request.getGateway())
                .status(PaymentStatus.PENDING)
                .transactionId(generateTransactionId())
                .build();

        payment = paymentRepository.save(payment);

        PaymentInitiateResponse response = PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .gateway(request.getGateway())
                .transactionId(payment.getTransactionId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .success(true)
                .message("Payment initiated successfully")
                .build();

        if (request.getGateway() == PaymentGateway.RAZORPAY) {

            // Fetch user details using Feign Client
          UserDTO userDTO = userClient.getUserById(request.getUserId());

            //  Create Razorpay payment link

            PaymentLinkResponse paymentLinkResponse = razorpayService.createPaymentLink(
                    userDTO , payment
            );

            response.setRazorpayOrderId(paymentLinkResponse.getPayment_link_id());
            response.setCheckoutUrl(paymentLinkResponse.getPayment_link_url());

            //  Set payment link in response
        }

        return response;
    }

    private String generateTransactionId() {

        return "TXN-"
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    @Override
    public PaymentDTO verifyPayment(PaymentVerifyRequest request) throws Exception {

        JSONObject paymentDetails = razorpayService.fetchPaymentDetails(
                request.getRazorpayPaymentId()
        );

        String status = paymentDetails.optString("status");

        JSONObject notes = paymentDetails.getJSONObject("notes");

        Long paymentId = Long.parseLong(
                notes.optString("payment_id")
        );

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("payment not found"));

        boolean isValid = "captured".equalsIgnoreCase(status);

        if (isValid) {

            if (payment.getProvider() == PaymentGateway.RAZORPAY) {
                payment.setProviderPaymentId(request.getRazorpayPaymentId());
            }

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            paymentRepository.save(payment);

            //  publish payment success kafka event
            paymentEventProducer.sendPaymentCompleted(payment);

        } else {

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment verification failed");

            paymentRepository.save(payment);

            // publish payment failed kafka event
            paymentEventProducer.sendPaymentFailed(payment);
        }

        return PaymentMapper.toDTO(payment);
    }

    @Override
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {

        return paymentRepository.findAll(pageable)
                .map(PaymentMapper::toDTO);
    }

    @Override
    public Map<Long, PaymentDTO> getPaymentsByBookingIds(List<Long> bookingIds) {

        return paymentRepository.findByBookingIdIn(bookingIds)
                .stream()
                .collect(Collectors.toMap(
                        Payment::getId,
                        PaymentMapper::toDTO
                ));
    }
}