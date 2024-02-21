package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.project.domain.model.*;

import static java.util.Objects.isNull;

public interface OldBillingProfileMapper {
    static CompanyBillingProfileResponse companyDomainToResponse(final OldCompanyBillingProfile companyBillingProfile) {
        return new CompanyBillingProfileResponse()
                .address(companyBillingProfile.getAddress())
                .country(companyBillingProfile.getOldCountry() == null ? null :
                        companyBillingProfile.getOldCountry().display().orElse(companyBillingProfile.getOldCountry().iso3Code()))
                .countryCode(companyBillingProfile.getOldCountry() == null ? null : companyBillingProfile.getOldCountry().iso3Code())
                .id(companyBillingProfile.getId())
                .status(verificationStatusToResponse(companyBillingProfile.getStatus()))
                .name(companyBillingProfile.getName())
                .euVATNumber(companyBillingProfile.getEuVATNumber())
                .usEntity(companyBillingProfile.getUsEntity())
                .registrationNumber(companyBillingProfile.getRegistrationNumber())
                .subjectToEuropeVAT(companyBillingProfile.getSubjectToEuropeVAT())
                .registrationDate(DateMapper.toZoneDateTime(companyBillingProfile.getRegistrationDate()));
    }

    static IndividualBillingProfileResponse individualDomainToResponse(final OldIndividualBillingProfile individualBillingProfile) {
        return new IndividualBillingProfileResponse()
                .status(verificationStatusToResponse(individualBillingProfile.getStatus()))
                .id(individualBillingProfile.getId())
                .address(individualBillingProfile.getAddress())
                .birthdate(DateMapper.toZoneDateTime(individualBillingProfile.getBirthdate()))
                .country(individualBillingProfile.getOldCountry() == null ? null :
                        individualBillingProfile.getOldCountry().display().orElse(individualBillingProfile.getOldCountry().iso3Code()))
                .countryCode(individualBillingProfile.getOldCountry() == null ? null : individualBillingProfile.getOldCountry().iso3Code())
                .address(individualBillingProfile.getAddress())
                .idDocumentNumber(individualBillingProfile.getIdDocumentNumber())
                .firstName(individualBillingProfile.getFirstName())
                .lastName(individualBillingProfile.getLastName())
                .validUntil(DateMapper.toZoneDateTime(individualBillingProfile.getValidUntil()))
                .usCitizen(individualBillingProfile.getUsCitizen())
                .idDocumentCountryCode(individualBillingProfile.getIdDocumentCountryCode())
                .idDocumentType(idDocumentTypeToResponse(individualBillingProfile.getIdDocumentType()));
    }

    static VerificationStatus verificationStatusToResponse(final OldVerificationStatus oldVerificationStatus) {
        return switch (oldVerificationStatus) {
            case CLOSED -> VerificationStatus.CLOSED;
            case REJECTED -> VerificationStatus.REJECTED;
            case STARTED -> VerificationStatus.STARTED;
            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
            case VERIFIED -> VerificationStatus.VERIFIED;
        };
    }

    static IndividualBillingProfileResponse.IdDocumentTypeEnum idDocumentTypeToResponse(final OldIndividualBillingProfile.OldIdDocumentTypeEnum oldIdDocumentTypeEnum) {
        return isNull(oldIdDocumentTypeEnum) ? null
                : switch (oldIdDocumentTypeEnum) {
            case ID_CARD -> IndividualBillingProfileResponse.IdDocumentTypeEnum.ID_CARD;
            case PASSPORT -> IndividualBillingProfileResponse.IdDocumentTypeEnum.PASSPORT;
            case DRIVER_LICENSE -> IndividualBillingProfileResponse.IdDocumentTypeEnum.DRIVER_LICENSE;
            case RESIDENCE_PERMIT -> IndividualBillingProfileResponse.IdDocumentTypeEnum.RESIDENCE_PERMIT;
        };
    }

    static OldBillingProfileType billingProfileToDomain(final BillingProfileTypeRequest billingProfileTypeRequest) {
        return switch (billingProfileTypeRequest.getType()) {
            case COMPANY -> OldBillingProfileType.COMPANY;
            case INDIVIDUAL -> OldBillingProfileType.INDIVIDUAL;
            case SELF_EMPLOYED -> OldBillingProfileType.COMPANY;
        };
    }

    static ShortBillingProfileResponse map(OldBillingProfile oldBillingProfile) {
        return new ShortBillingProfileResponse()
                .id(oldBillingProfile.id())
                .name(oldBillingProfile.name())
                .type(map(oldBillingProfile.type()))
                .rewardCount(oldBillingProfile.rewardCount())
                .invoiceMandateAccepted(oldBillingProfile.invoiceMandateAccepted());
    }

    static onlydust.com.marketplace.api.contract.model.BillingProfileType map(OldBillingProfileType type) {
        return switch (type) {
            case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
            case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
        };
    }

}
