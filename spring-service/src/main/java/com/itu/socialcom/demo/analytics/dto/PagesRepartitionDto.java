package com.itu.socialcom.demo.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PagesRepartitionDto {
    @JsonProperty("dummy_id")
    Integer dummyId;
    @JsonProperty("total_percentage")
    Double totalPercentage;
    @JsonProperty("total")
    Double total;
    @JsonProperty("page_title")
    String pageTitle;
    @JsonProperty("id_sp")
    Integer idSp;
    @JsonProperty("id_managed_pages")
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
