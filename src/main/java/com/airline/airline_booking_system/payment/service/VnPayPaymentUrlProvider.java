package com.airline.airline_booking_system.payment.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.airline.airline_booking_system.payment.dto.PendingPaymentData;

@Component
public class VnPayPaymentUrlProvider implements PaymentUrlProvider {
    @Value("${vnpay.url}")
    private String paymentUrl;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.curr-code}")
    private String currCode;

    @Value("${vnpay.locale}")
    private String locale;

    @Value("${vnpay.order-type}")
    private String orderType;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String createPaymentUrl(PendingPaymentData paymentData, String clientIp) {
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", convertAmount(paymentData.amount()));
        params.put("vnp_CurrCode", currCode);
        params.put("vnp_TxnRef", paymentData.transactionId());
        params.put("vnp_OrderInfo", "Payment for booking " + paymentData.bookingId());
        params.put("vnp_OrderType", orderType);
        params.put("vnp_Locale", locale);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));

        String hashData = buildHashData(params);

        String secureHash = hmacSHA512(hashSecret, hashData);

        String queryString = buildQueryString(params);

        return paymentUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    private String convertAmount(BigDecimal amount) {
        return amount.movePointRight(2).longValueExact() + "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

            hmac.init(secretKey);

            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b & 0xff));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate VNPay secure hash", e);
        }
    }

    private String buildHashData(Map<String, String> params) {
        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();

            if (value == null || value.isEmpty()) {
                continue;
            }

            if (!hashData.isEmpty()) {
                hashData.append("&");
            }

            hashData.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(value));
        }

        return hashData.toString();
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();

            if (value == null || value.isEmpty()) {
                continue;
            }

            if (!query.isEmpty()) {
                query.append("&");
            }

            query.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(value));
        }

        return query.toString();
    }
}
