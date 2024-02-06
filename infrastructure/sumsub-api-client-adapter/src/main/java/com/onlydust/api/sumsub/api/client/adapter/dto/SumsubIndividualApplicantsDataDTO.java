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
public class SumsubIndividualApplicantsDataDTO {

    @JsonProperty("info")
    InfoDTO info;
    @JsonProperty("questionnaires")
    List<QuestionnaireDTO> questionnaires;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoDTO {
        @JsonProperty("firstName")
        String firstName;
        @JsonProperty("lastName")
        String lastName;
        @JsonProperty("dob")
        String birthdate;
        @JsonProperty("addresses")
        List<AddressDTO> addresses;
        @JsonProperty("idDocs")
        List<IdDocumentDTO> idDocuments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressDTO {
        @JsonProperty("formattedAddress")
        String formattedAddress;
        @JsonProperty("country")
        String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdDocumentDTO {
        @JsonProperty("idDocType")
        String type;
        @JsonProperty("country")
        String country;
        @JsonProperty("validUntil")
        String validUntil;
        @JsonProperty("number")
        String number;
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
