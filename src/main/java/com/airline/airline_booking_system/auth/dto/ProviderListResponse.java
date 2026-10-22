package com.airline.airline_booking_system.auth.dto;

import java.util.List;

public record ProviderListResponse(List<ProviderItem> providers, boolean hasPassword) {
    public record ProviderItem(String provider, boolean linked) { }
}
