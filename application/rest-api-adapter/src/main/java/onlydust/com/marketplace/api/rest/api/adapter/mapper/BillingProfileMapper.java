package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.BillingProfile;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;

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

    static ShortBillingProfileResponse map(BillingProfile billingProfile) {
        return new ShortBillingProfileResponse()
                .id(billingProfile.id())
                .name(billingProfile.name())
                .type(map(billingProfile.type()))
                .rewardCount(billingProfile.rewardCount());
    }

    static BillingProfileInvoicesPageResponse map(Page<Invoice> page, Integer pageIndex) {
        return new BillingProfileInvoicesPageResponse()
                .invoices(page.getContent().stream().map(BillingProfileMapper::map).toList())
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()))
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static BillingProfileInvoicesPageItemResponse map(Invoice invoice) {
        return new BillingProfileInvoicesPageItemResponse()
                .id(invoice.id().value())
                .name(invoice.name())
                .generationDate(invoice.createdAt())
                .totalAfterTax(map(invoice.totalAfterTax()))
                .status(map(invoice.status()));
    }

    static NewMoney map(Money money) {
        return new NewMoney()
                .amount(money.getValue())
                .currency(map(money.getCurrency()));
    }

    static CurrencyContract map(Currency currency) {
        return CurrencyContract.fromValue(currency.code().toString());
    }

    static InvoiceStatus map(Invoice.Status status) {
        return switch (status) {
            case PROCESSING -> InvoiceStatus.PROCESSING;
            case COMPLETE -> InvoiceStatus.COMPLETE;
            case REJECTED -> InvoiceStatus.REJECTED;
        };
    }

    static onlydust.com.marketplace.api.contract.model.BillingProfileType map(BillingProfileType type) {
        return switch (type) {
            case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
            case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
        };
    }
}
