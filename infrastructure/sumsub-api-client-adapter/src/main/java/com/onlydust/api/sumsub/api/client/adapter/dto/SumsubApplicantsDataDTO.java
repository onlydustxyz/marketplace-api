package com.onlydust.api.sumsub.api.client.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.onlydust.api.sumsub.api.client.adapter.SumsubClientProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.nonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SumsubApplicantsDataDTO {

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

    public IndividualBillingProfile updateIndividualBillingProfile(final IndividualBillingProfile individualBillingProfile,
                                                                   final SumsubClientProperties sumsubClientProperties) {
        IndividualBillingProfile updatedIndividualBillingProfile = individualBillingProfile;
        if (nonNull(this.info)) {
            updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                    .firstName(this.info.firstName)
                    .lastName(this.info.lastName)
                    .birthdate(mapDate(this.info.birthdate))
                    .build();
            if (nonNull(this.info.addresses) && !this.info.addresses.isEmpty()) {
                updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                        .address(this.info.addresses.get(0).formattedAddress)
                        .build();
                final String countryIso3 = this.info.addresses.get(0).country;
                if (nonNull(countryIso3) && COUNTRY_NAME_MAPPED_TO_ISO3_CODE.containsKey(countryIso3)) {
                    updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                            .country(COUNTRY_NAME_MAPPED_TO_ISO3_CODE.get(countryIso3))
                            .build();
                }
            }
            if (nonNull(this.info.idDocuments) && !this.info.idDocuments.isEmpty()) {
                final Optional<IdDocumentDTO> optionalIdDocument = this.info.idDocuments.stream()
                        .filter(idDocumentDTO -> List.of("PASSPORT",
                                "ID_CARD",
                                "RESIDENCE_PERMIT",
                                "DRIVER_LICENSE").contains(idDocumentDTO.getType()))
                        .findFirst();
                if (optionalIdDocument.isPresent()) {
                    final IdDocumentDTO idDocumentDTO = optionalIdDocument.get();
                    updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                            .idDocumentType(switch (idDocumentDTO.getType()) {
                                case "PASSPORT" -> IndividualBillingProfile.IdDocumentTypeEnum.PASSPORT;
                                case "ID_CARD" -> IndividualBillingProfile.IdDocumentTypeEnum.ID_CARD;
                                case "RESIDENCE_PERMIT" -> IndividualBillingProfile.IdDocumentTypeEnum.RESIDENCE_PERMIT;
                                case "DRIVER_LICENSE" -> IndividualBillingProfile.IdDocumentTypeEnum.DRIVER_LICENSE;
                                default -> null;
                            })
                            .idDocumentNumber(idDocumentDTO.getNumber())
                            .validUntil(mapDate(idDocumentDTO.getValidUntil()))
                            .idDocumentCountryCode(idDocumentDTO.getCountry())
                            .build();
                }
            }
            if (nonNull(this.questionnaires) && !this.questionnaires.isEmpty()) {
                final Optional<QuestionnaireDTO> optionalQuestionnaireDTO = this.questionnaires.stream()
                        .filter(q -> q.id.equals(sumsubClientProperties.getKycQuestionnaireName()))
                        .findFirst();
                if (optionalQuestionnaireDTO.isPresent()) {
                    final QuestionnaireDTO questionnaireDTO = optionalQuestionnaireDTO.get();
                    if (nonNull(questionnaireDTO.getSections()) && !questionnaireDTO.getSections().isEmpty() && questionnaireDTO.getSections().has(
                            "personalStatusVerifi")) {
                        final JsonNode personalStatusVerification = questionnaireDTO.getSections().get("personalStatusVerifi");
                        if (personalStatusVerification.has("items") && personalStatusVerification.get("items").has("areYouConsideredAUsP") &&
                            personalStatusVerification.get("items").get("areYouConsideredAUsP").has("value")) {
                            updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                                    .usCitizen(switch (personalStatusVerification.get("items").get("areYouConsideredAUsP").get("value").textValue()) {
                                        case "no" -> false;
                                        case "yes" -> true;
                                        default -> null;
                                    })
                                    .build();
                        }
                    }
                }
            }
        }

        return updatedIndividualBillingProfile;
    }


    private static Date mapDate(final String dateString) {
        try {
            if (nonNull(dateString) && !dateString.isEmpty()) {
                return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            }
            return null;
        } catch (ParseException e) {
            throw OnlyDustException.internalServerError(String.format("Failed to parse date %s to format yyyy-MM-dd", dateString));
        }
    }

    private static final Map<String, String> COUNTRY_NAME_MAPPED_TO_ISO3_CODE = new HashMap<>();

    {
        for (String isoCountry : Locale.getISOCountries()) {
            final Locale locale = new Locale("", isoCountry);
            COUNTRY_NAME_MAPPED_TO_ISO3_CODE.put(locale.getISO3Country(), locale.getDisplayCountry(Locale.ENGLISH));
        }
    }
}
