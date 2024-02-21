package com.onlydust.api.sumsub.api.client.adapter.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.onlydust.api.sumsub.api.client.adapter.SumsubClientProperties;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubCompanyApplicantsDataDTO;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubCompanyChecksDTO;
import com.onlydust.api.sumsub.api.client.adapter.dto.SumsubIndividualApplicantsDataDTO;
import onlydust.com.marketplace.project.domain.model.OldCountry;
import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public class SumsubResponseMapper {

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

    public OldIndividualBillingProfile updateIndividualBillingProfile(final SumsubIndividualApplicantsDataDTO sumsubIndividualApplicantsDataDTO,
                                                                      final OldIndividualBillingProfile individualBillingProfile,
                                                                      final SumsubClientProperties sumsubClientProperties) {

        OldIndividualBillingProfile updatedIndividualBillingProfile = individualBillingProfile;
        if (nonNull(sumsubIndividualApplicantsDataDTO.getInfo())) {
            updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                    .firstName(sumsubIndividualApplicantsDataDTO.getInfo().getFirstName())
                    .lastName(sumsubIndividualApplicantsDataDTO.getInfo().getLastName())
                    .birthdate(mapDate(sumsubIndividualApplicantsDataDTO.getInfo().getBirthdate()))
                    .build();
            if (nonNull(sumsubIndividualApplicantsDataDTO.getInfo().getAddresses()) && !sumsubIndividualApplicantsDataDTO.getInfo().getAddresses().isEmpty()) {
                updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                        .address(sumsubIndividualApplicantsDataDTO.getInfo().getAddresses().get(0).getFormattedAddress())
                        .build();
                final String countryIso3 = sumsubIndividualApplicantsDataDTO.getInfo().getAddresses().get(0).getCountry();
                if (nonNull(countryIso3)) {
                    updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                            .oldCountry(OldCountry.fromIso3(countryIso3))
                            .build();
                }
            }
            if (nonNull(sumsubIndividualApplicantsDataDTO.getInfo().getIdDocuments()) && !sumsubIndividualApplicantsDataDTO.getInfo().getIdDocuments().isEmpty()) {
                final Optional<SumsubIndividualApplicantsDataDTO.IdDocumentDTO> optionalIdDocument =
                        sumsubIndividualApplicantsDataDTO.getInfo().getIdDocuments().stream()
                                .filter(idDocumentDTO -> List.of("PASSPORT",
                                        "ID_CARD",
                                        "RESIDENCE_PERMIT",
                                        "DRIVERS").contains(idDocumentDTO.getType()))
                                .findFirst();
                if (optionalIdDocument.isPresent()) {
                    final SumsubIndividualApplicantsDataDTO.IdDocumentDTO idDocumentDTO = optionalIdDocument.get();
                    updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                            .idDocumentType(switch (idDocumentDTO.getType()) {
                                case "PASSPORT" -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.PASSPORT;
                                case "ID_CARD" -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.ID_CARD;
                                case "RESIDENCE_PERMIT" -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.RESIDENCE_PERMIT;
                                case "DRIVERS" -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.DRIVER_LICENSE;
                                default -> null;
                            })
                            .idDocumentNumber(idDocumentDTO.getNumber())
                            .validUntil(mapDate(idDocumentDTO.getValidUntil()))
                            .idDocumentCountryCode(idDocumentDTO.getCountry())
                            .build();
                }
            }
            if (nonNull(sumsubIndividualApplicantsDataDTO.getQuestionnaires()) && !sumsubIndividualApplicantsDataDTO.getQuestionnaires().isEmpty()) {
                final Optional<SumsubIndividualApplicantsDataDTO.QuestionnaireDTO> optionalQuestionnaireDTO =
                        sumsubIndividualApplicantsDataDTO.getQuestionnaires().stream()
                                .filter(q -> q.getId().equals(sumsubClientProperties.getKycQuestionnaireName()))
                                .findFirst();
                if (optionalQuestionnaireDTO.isPresent()) {
                    final SumsubIndividualApplicantsDataDTO.QuestionnaireDTO questionnaireDTO = optionalQuestionnaireDTO.get();
                    if (nonNull(questionnaireDTO.getSections()) && !questionnaireDTO.getSections().isEmpty() && questionnaireDTO.getSections().has(
                            "personalStatusVerifi")) {
                        final JsonNode personalStatusVerification = questionnaireDTO.getSections().get("personalStatusVerifi");
                        if (personalStatusVerification.has("items") && personalStatusVerification.get("items").has("areYouConsideredAUsP") &&
                            personalStatusVerification.get("items").get("areYouConsideredAUsP").has("value")) {
                            updatedIndividualBillingProfile = updatedIndividualBillingProfile.toBuilder()
                                    .usCitizen(mapBoolean(personalStatusVerification.get("items").get("areYouConsideredAUsP").get("value").textValue()))
                                    .build();
                        }
                    }
                }
            }
        }
        return updatedIndividualBillingProfile;
    }

    public OldCompanyBillingProfile updateCompanyBillingProfile(SumsubCompanyApplicantsDataDTO applicantsData,
                                                                SumsubCompanyChecksDTO companyChecks,
                                                                OldCompanyBillingProfile companyBillingProfile,
                                                                SumsubClientProperties sumsubClientProperties) {
        OldCompanyBillingProfile updatedCompanyBillingProfile = companyBillingProfile;
        if (nonNull(applicantsData.getInfo()) && nonNull(applicantsData.getInfo()
                .getCompanyInfo())) {
            updatedCompanyBillingProfile = updatedCompanyBillingProfile.toBuilder()
                    .name(applicantsData.getInfo().getCompanyInfo().getName())
                    .registrationNumber(applicantsData.getInfo().getCompanyInfo().getRegistrationNumber())
                    .oldCountry(OldCountry.fromIso3(applicantsData.getInfo().getCompanyInfo().getCountry()))
                    .build();
        }
        if (nonNull(applicantsData.getQuestionnaires()) && !applicantsData.getQuestionnaires().isEmpty()) {
            final Optional<SumsubCompanyApplicantsDataDTO.QuestionnaireDTO> optionalQuestionnaireDTO = applicantsData.getQuestionnaires().stream()
                    .filter(q -> q.getId().equals(sumsubClientProperties.getKybQuestionnaireName()))
                    .findFirst();
            if (optionalQuestionnaireDTO.isPresent()) {
                final SumsubCompanyApplicantsDataDTO.QuestionnaireDTO questionnaireDTO = optionalQuestionnaireDTO.get();
                if (nonNull(questionnaireDTO.getSections()) && !questionnaireDTO.getSections().isEmpty()
                    && questionnaireDTO.getSections().has("usAndEuropeanComplia") && questionnaireDTO.getSections().get("usAndEuropeanComplia").has("items")) {
                    final JsonNode items = questionnaireDTO.getSections().get("usAndEuropeanComplia").get("items");
                    if (items.has("whatIsYourEuVatRegis") && items.get("whatIsYourEuVatRegis").has("value")) {
                        updatedCompanyBillingProfile = updatedCompanyBillingProfile.toBuilder()
                                .euVATNumber(items.get("whatIsYourEuVatRegis").get("value").asText())
                                .build();
                    }
                    if (items.has("isYourEntityAUsPerso") && items.get("isYourEntityAUsPerso").has("value")) {
                        updatedCompanyBillingProfile = updatedCompanyBillingProfile.toBuilder()
                                .usEntity(mapBoolean(items.get("isYourEntityAUsPerso").get("value").asText()))
                                .build();
                    }
                    if (items.has("isYourCompanySubject") && items.get("isYourCompanySubject").has("value")) {
                        updatedCompanyBillingProfile = updatedCompanyBillingProfile.toBuilder()
                                .subjectToEuropeVAT(mapBoolean(items.get("isYourCompanySubject").get("value").asText()))
                                .build();
                    }

                }
            }
        }
        if (nonNull(companyChecks)) {
            if (nonNull(companyChecks.getChecks()) && !companyChecks.getChecks().isEmpty()) {
                final Optional<SumsubCompanyChecksDTO.CompanyChecksDTO> optionalCompanyChecksDTO = companyChecks.getChecks().stream()
                        .filter(c -> nonNull(c.getType()) && c.getType().equals("COMPANY"))
                        .findFirst();
                if (optionalCompanyChecksDTO.isPresent()) {
                    final SumsubCompanyChecksDTO.CompanyChecksDTO companyChecksDTO = optionalCompanyChecksDTO.get();
                    if (nonNull(companyChecksDTO.getInfo())) {
                        updatedCompanyBillingProfile = updatedCompanyBillingProfile.toBuilder()
                                .address(companyChecksDTO.getInfo().getOfficeAddress())
                                .registrationDate(mapDate(companyChecksDTO.getInfo().getIncorporatedOn()))
                                .build();
                    }
                }
            }
        }
        return updatedCompanyBillingProfile;
    }

    private static Boolean mapBoolean(final String boolStr) {
        return switch (boolStr) {
            case "no" -> false;
            case "yes" -> true;
            default -> null;
        };
    }
}
