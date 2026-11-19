package com.airline.airline_booking_system.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.airline.airline_booking_system.payment.dto.PendingPaymentData;
import com.airline.airline_booking_system.payment.service.VnPayPaymentUrlProvider;

public class VnPayPaymentUrlProviderTest {
    private VnPayPaymentUrlProvider provider;

    @BeforeEach
    void setUp() {
        provider = new VnPayPaymentUrlProvider();

        ReflectionTestUtils.setField(provider, "paymentUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(provider, "tmnCode", "TESTCODE");
        ReflectionTestUtils.setField(provider, "hashSecret", "TESTSECRET");
        ReflectionTestUtils.setField(
                provider, "returnUrl",
                "http://localhost:8080/api/v1/payments/vnpay/return");

        ReflectionTestUtils.setField(provider, "version", "2.1.0");
        ReflectionTestUtils.setField(provider, "command", "pay");
        ReflectionTestUtils.setField(provider, "currCode", "VND");
        ReflectionTestUtils.setField(provider, "locale", "vn");
        ReflectionTestUtils.setField(provider, "orderType", "other");
    }

    @Test
    void createPaymentUrl_shouldGenerateValidUrl() {
        PendingPaymentData paymentData = new PendingPaymentData("payment-001",
                "booking-001",
                new BigDecimal("1500000"),
                "VNPAY",
                "PENDING",
                "transaction001",
                null);

        String result = provider.createPaymentUrl(paymentData, "127.0.0.1");

        assertNotNull(result);

        assertTrue(result.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(result.contains("vnp_TxnRef=transaction001"));
        assertTrue(result.contains("vnp_Amount=150000000"));
        assertTrue(result.contains("vnp_TmnCode=TESTCODE"));
        assertTrue(result.contains("vnp_CurrCode=VND"));
        assertTrue(result.contains("vnp_IpAddr=127.0.0.1"));
        assertTrue(result.contains("vnp_SecureHash="));
    }

    @Test
    void createPaymentUrl_shouldGenerateHmacSHA512Signature() {

        PendingPaymentData paymentData = new PendingPaymentData(
                "payment-002",
                "booking-002",
                new BigDecimal("200000"),
                "VNPAY",
                "PENDING",
                "transaction002",
                null);

        String result = provider.createPaymentUrl(
                paymentData,
                "127.0.0.1");

        String secureHash = result.substring(
                result.indexOf("vnp_SecureHash=")
                        + "vnp_SecureHash=".length());

        assertEquals(128, secureHash.length());
        assertTrue(secureHash.matches("[0-9a-f]{128}"));
    }
}
