package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.BillingProfileTypeRequest;
import onlydust.com.marketplace.api.contract.model.CompanyBillingProfileResponse;
import onlydust.com.marketplace.api.contract.model.IndividualBillingProfileResponse;
import onlydust.com.marketplace.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;

import static java.util.Objects.isNull;

public interface BillingProfileMapper {

    static CompanyBillingProfileResponse companyDomainToResponse(final CompanyBillingProfile companyBillingProfile) {
        return new CompanyBillingProfileResponse()
                .address(companyBillingProfile.getAddress())
                .country(companyBillingProfile.getCountry())
                .id(companyBillingProfile.getId())
                .status(verificationStatusToResponse(companyBillingProfile.getStatus()))
                .name(companyBillingProfile.getName())
                .euVATNumber(companyBillingProfile.getEuVATNumber())
                .usEntity(companyBillingProfile.getUsEntity())
                .registrationNumber(companyBillingProfile.getRegistrationNumber())
                .subjectToEuropeVAT(companyBillingProfile.getSubjectToEuropeVAT())
                .registrationDate(DateMapper.toZoneDateTime(companyBillingProfile.getRegistrationDate()));
    }

    static IndividualBillingProfileResponse individualDomainToResponse(final IndividualBillingProfile individualBillingProfile) {
        return new IndividualBillingProfileResponse()
                .status(verificationStatusToResponse(individualBillingProfile.getStatus()))
                .id(individualBillingProfile.getId())
                .address(individualBillingProfile.getAddress())
                .birthdate(DateMapper.toZoneDateTime(individualBillingProfile.getBirthdate()))
                .country(individualBillingProfile.getCountry())
                .address(individualBillingProfile.getAddress())
                .idDocumentNumber(individualBillingProfile.getIdDocumentNumber())
                .firstName(individualBillingProfile.getFirstName())
                .lastName(individualBillingProfile.getLastName())
                .validUntil(DateMapper.toZoneDateTime(individualBillingProfile.getValidUntil()))
                .usCitizen(individualBillingProfile.getUsCitizen())
                .idDocumentCountryCode(individualBillingProfile.getIdDocumentCountryCode())
                .idDocumentType(idDocumentTypeToResponse(individualBillingProfile.getIdDocumentType()));
    }

    static VerificationStatus verificationStatusToResponse(final onlydust.com.marketplace.api.domain.model.VerificationStatus verificationStatus) {
        return switch (verificationStatus) {
            case CLOSED -> VerificationStatus.CLOSED;
            case INVALIDATED -> VerificationStatus.INVALIDATED;
            case REJECTED -> VerificationStatus.REJECTED;
            case STARTED -> VerificationStatus.STARTED;
            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
            case VERIFIED -> VerificationStatus.VERIFIED;
        };
    }

    static IndividualBillingProfileResponse.IdDocumentTypeEnum idDocumentTypeToResponse(final IndividualBillingProfile.IdDocumentTypeEnum idDocumentTypeEnum) {
        return isNull(idDocumentTypeEnum) ? null
                : switch (idDocumentTypeEnum) {
            case ID_CARD -> IndividualBillingProfileResponse.IdDocumentTypeEnum.ID_CARD;
            case PASSPORT -> IndividualBillingProfileResponse.IdDocumentTypeEnum.PASSPORT;
            case DRIVER_LICENSE -> IndividualBillingProfileResponse.IdDocumentTypeEnum.DRIVER_LICENSE;
            case RESIDENCE_PERMIT -> IndividualBillingProfileResponse.IdDocumentTypeEnum.RESIDENCE_PERMIT;
        };
    }

    static BillingProfileType billingProfileToDomain(final BillingProfileTypeRequest billingProfileTypeRequest) {
        return switch (billingProfileTypeRequest.getType()) {
            case COMPANY -> BillingProfileType.COMPANY;
            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
        };
    }
}
