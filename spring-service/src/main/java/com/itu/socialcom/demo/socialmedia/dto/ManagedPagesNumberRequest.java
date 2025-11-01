package com.itu.socialcom.demo.socialmedia.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManagedPagesNumberRequest {
    @NotNull
    private Long idSpn;
    @NotNull
    private Long idMp;
    @NotNull
    private Long idPm; // payment method id (e.g., 1 for mvola)
}
