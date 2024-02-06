package com.onlydust.api.sumsub.api.client.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SumsubCompanyApplicantsDataDTO {

    @JsonProperty("id")
    String id;
    @JsonProperty("info")
    InfoDTO info;
    @JsonProperty("questionnaires")
    List<QuestionnaireDTO> questionnaires;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoDTO {
        @JsonProperty("companyInfo")
        CompanyInfoDTO companyInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyInfoDTO {
        @JsonProperty("companyName")
        String name;
        @JsonProperty("registrationNumber")
        String registrationNumber;
        @JsonProperty("country")
        String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionnaireDTO {
        @JsonProperty("id")
        String id;
        JsonNode sections;
    }
}
