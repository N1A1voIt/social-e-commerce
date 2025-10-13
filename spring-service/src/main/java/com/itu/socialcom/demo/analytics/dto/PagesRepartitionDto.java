package com.itu.socialcom.demo.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PagesRepartitionDto {
    Integer dummyId;
    Double totalPercentage;
    Double total;
    String pageTitle;
    Integer idSp;
    Integer idManagedPages;
    public PagesRepartitionDto() {}
    public PagesRepartitionDto(Integer dummyId, Double totalPercentage, Double total, String pageTitle, Integer idSp, Integer idManagedPages) {
        this.dummyId = dummyId;
        this.totalPercentage = totalPercentage;
        this.total = total;
        this.pageTitle = pageTitle;
        this.idSp = idSp;
        this.idManagedPages = idManagedPages;
    }
}
