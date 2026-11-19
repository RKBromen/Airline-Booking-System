package com.airline.airline_booking_system.payment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.airline.airline_booking_system.payment.service.VnPaySignatureService;

public class VnPaySignatureServiceTest {
    private VnPaySignatureService service;

    private static final String SECRET = "TESTSECRET";

    @BeforeEach
    void setUp() {
        service = new VnPaySignatureService();
        ReflectionTestUtils.setField(service, "hashSecret", SECRET);
    }

    @Test
    void verifySignature_shouldReturnTrue_whenSignatureIsValid() {
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Amount", "150000000");
        params.put("vnp_TxnRef", "transaction001");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");

        params.put("vnp_SecureHash", generateSignature(params));

        assertTrue(service.verifySignature(params));
    }

    @Test
    void verifySignature_shouldReturnFalse_whenSignatureIsInvalid() {
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Amount", "150000000");
        params.put("vnp_TxnRef", "transaction001");
        params.put("vnp_SecureHash", "invalid");

        assertFalse(service.verifySignature(params));
    }

    @Test
    void verifySignature_shouldReturnFalse_whenSignatureIsMissing() {
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Amount", "150000000");

        assertFalse(service.verifySignature(params));
    }

    @Test
    void verifySignature_shouldReturnFalse_whenDataIsModified() {
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Amount", "150000000");
        params.put("vnp_TxnRef", "transaction001");
        params.put("vnp_SecureHash", generateSignature(params));

        params.put("vnp_Amount", "200000000");

        assertFalse(service.verifySignature(params));
    }

    private String generateSignature(Map<String, String> params) {
        try {
            Map<String, String> sorted = new TreeMap<>(params);

            StringBuilder data = new StringBuilder();

            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                if (entry.getKey().equals("vnp_SecureHash")) {
                    continue;
                }

                if (!data.isEmpty()) {
                    data.append("&");
                }

                data.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
            }

            Mac mac = Mac.getInstance("HmacSHA512");

            mac.init(new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"));

            byte[] hash = mac.doFinal(
                    data.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b & 0xff));
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
