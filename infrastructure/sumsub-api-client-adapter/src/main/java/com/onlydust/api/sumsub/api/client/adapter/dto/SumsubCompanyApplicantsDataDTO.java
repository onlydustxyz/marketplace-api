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
    @JsonProperty("review")
    ReviewDTO review;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ReviewDTO {
        @JsonProperty("reviewResult")
        ReviewResultDTO result;
        @JsonProperty("reviewStatus")
        String status;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ReviewResultDTO {
        @JsonProperty("moderationComment")
        String moderationComment;
        @JsonProperty("clientComment")
        String clientComment;
        @JsonProperty("reviewAnswer")
        String reviewAnswer;
        @JsonProperty("rejectLabels")
        List<String> rejectLabels;
        @JsonProperty("reviewRejectType")
        String reviewRejectType;
        @JsonProperty("buttonIds")
        List<String> buttonIds;
    }
}
