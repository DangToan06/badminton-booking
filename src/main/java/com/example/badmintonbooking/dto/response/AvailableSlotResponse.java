package com.example.badmintonbooking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AvailableSlotResponse {

    private Long courtId;
    private String courtName;
    private String date;
    private BigDecimal pricePerSlot;
    private List<SlotInfo> slots;

    @Getter
    @Builder
    public static class SlotInfo {
        private String timeSlot;
        private boolean available;
    }
}