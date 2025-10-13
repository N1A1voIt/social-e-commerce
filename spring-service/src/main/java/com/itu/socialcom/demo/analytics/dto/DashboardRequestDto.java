package com.itu.socialcom.demo.analytics.dto;

import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
public class DashboardRequestDto {
    LocalDateTime startDate;
    LocalDateTime endDate;

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
}
