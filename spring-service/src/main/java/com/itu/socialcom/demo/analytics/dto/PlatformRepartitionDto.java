package com.itu.socialcom.demo.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformRepartitionDto {
    Integer dummyId;
    Double totalPercentage;
    Double total;
    Integer idSp;
    
    public PlatformRepartitionDto() {}

    public PlatformRepartitionDto(Integer dummyId, Double totalPercentage, Double total, Integer idSp) {
        this.dummyId = dummyId;
        this.totalPercentage = totalPercentage;
        this.total = total;
        this.idSp = idSp;
    }
}
