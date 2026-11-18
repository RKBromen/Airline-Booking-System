package com.airline.airline_booking_system.payment.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.payment.dto.VnPayIPNResponse;
import com.airline.airline_booking_system.payment.service.VnPayIPNService;
import com.airline.airline_booking_system.payment.service.VnPaySignatureService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("api/v1/payments/vnpay")
@RequiredArgsConstructor
public class VnpayCallbackController {
    private final VnPaySignatureService signatureService;
    private final VnPayIPNService vnPayIPNService;

    @GetMapping("/return")
    public ResponseEntity<String> handleReturn(
            @RequestParam Map<String, String> params) {

        System.out.println("========== VNPAY RETURN ==========");

        params.forEach((key, value) -> {
            if (!key.equals("vnp_SecureHash")) {
                System.out.println(key + " = " + value);
            }
        });

        System.out.println("Received Hash: "
                + params.get("vnp_SecureHash"));

        boolean validSignature = signatureService.verifySignature(params);

        if (!validSignature) {
            return ResponseEntity.badRequest()
                    .body("Invalid VNPay signature");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(responseCode)
                && "00".equals(transactionStatus)) {
            return ResponseEntity.ok("Payment successful");
        }

        return ResponseEntity.ok("Payment failed");
    }

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<VnPayIPNResponse> handleIpn(
            @RequestParam Map<String, String> params) {

        VnPayIPNResponse response = vnPayIPNService.validateIpn(params);

        return ResponseEntity.ok(response);
    }
}
