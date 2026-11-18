package com.airline.airline_booking_system.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VnPaySignatureService {
    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    public boolean verifySignature(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        String receivedHash = params.get("vnp_SecureHash");

        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        try {
            String hashData = buildHashData(params);
            String calculatedHash = hmacSHA512(hashSecret, hashData);

            return MessageDigest.isEqual(
                    calculatedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII),
                    receivedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII));

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot verify VNPay signature", e);
        }
    }

    private String buildHashData(Map<String, String> params) {

        Map<String, String> sortedParams = new TreeMap<>();

        params.forEach((key, value) -> {
            if (key.startsWith("vnp_")
                    && !key.equals("vnp_SecureHash")
                    && !key.equals("vnp_SecureHashType")
                    && value != null
                    && !value.isEmpty()) {

                sortedParams.put(key, value);
            }
        });

        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {

            if (!hashData.isEmpty()) {
                hashData.append("&");
            }

            hashData.append(
                    URLEncoder.encode(
                            entry.getKey(),
                            StandardCharsets.UTF_8));

            hashData.append("=");

            hashData.append(
                    URLEncoder.encode(
                            entry.getValue(),
                            StandardCharsets.UTF_8));
        }

        return hashData.toString();
    }

    private String hmacSHA512(String key, String data)
            throws Exception {

        Mac hmac = Mac.getInstance("HmacSHA512");

        SecretKeySpec secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512");

        hmac.init(secretKey);

        byte[] hash = hmac.doFinal(
                data.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();
    }
}
