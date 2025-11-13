package com.itu.socialcom.demo.analytics.dto;

import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
public class DashboardRequestDto {
    LocalDateTime startDate;
    LocalDateTime endDate;
    String timeFrame;  // WEEKLY, MONTHLY, or YEARLY

    public LocalDateTime getStartDate() {
        if (startDate == null) {
            return LocalDateTime.now().minusYears(1990);
        }
        return startDate;
    }

    public LocalDateTime getEndDate() {
        if (endDate == null) {
            return LocalDateTime.now().plusYears(1990);
        }
        return endDate;
    }

    public String getTimeFrame() {
        if (timeFrame == null || timeFrame.isEmpty()) {
            return "WEEKLY";  // Default to WEEKLY if not specified
        }
        return timeFrame;
    }
}
