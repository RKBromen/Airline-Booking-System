package com.airline.airline_booking_system.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VnPayIPNResponse(@JsonProperty("RspCode") String rspCode, @JsonProperty("Message") String message) {

}
