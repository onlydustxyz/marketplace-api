package com.onlydust.api.sumsub.api.client.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SumsubCompanyChecksDTO {
    @JsonProperty("checks")
    List<CompanyChecksDTO> checks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyChecksDTO {
        @JsonProperty("checkType")
        String type;
        @JsonProperty("companyCheckInfo")
        CompanyCheckInfoDTO info;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyCheckInfoDTO {
        @JsonProperty("incorporatedOn")
        String incorporatedOn;
        @JsonProperty("officeAddress")
        String officeAddress;
    }
}
