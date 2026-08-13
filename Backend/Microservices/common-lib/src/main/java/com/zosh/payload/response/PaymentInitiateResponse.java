package com.zosh.payload.response;

import com.zosh.enums.PaymentGateway;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {

    private Long paymentId;
    private PaymentGateway gateway;
    private String transactionId;

    // Razorpay specific fields
    private String razorpayOrderId;

    private Double amount;

    private String description;

    // Frontend should redirect user to this URL for payment
    private String checkoutUrl;

    private String message;
    private Boolean success;
}
