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

import java.math.RoundingMode;

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

    static InvoicePreviewResponse map(Invoice preview) {
        return new InvoicePreviewResponse()
                .id(preview.id().value())
                .name(preview.name().value())
                .createdAt(preview.createdAt())
                .dueAt(preview.dueAt())
                .billingProfileType(map(preview.billingProfileType()))
                .individualBillingProfile(preview.personalInfo().map(BillingProfileMapper::map).orElse(null))
                .companyBillingProfile(preview.companyInfo().map(BillingProfileMapper::map).orElse(null))
                .destinationAccounts(new DestinationAccountResponse()
                        .bankAccount(preview.bankAccount().map(BillingProfileMapper::map).orElse(null))
                        .wallets(preview.wallets().stream().map(BillingProfileMapper::map).toList())
                )
                .rewards(preview.rewards().stream().map(BillingProfileMapper::map).toList())
                .totalBeforeTax(map(preview.totalBeforeTax()))
                .taxRate(preview.taxRate())
                .totalTax(map(preview.totalTax()))
                .totalAfterTax(map(preview.totalAfterTax()))
                ;
    }

    static InvoiceRewardItemResponse map(Invoice.Reward reward) {
        return new InvoiceRewardItemResponse()
                .id(reward.id().value())
                .date(reward.createdAt())
                .projectName(reward.projectName())
                .amount(toConvertibleMoney(reward.amount(), reward.base()))
                ;
    }

    static WalletResponse map(Invoice.Wallet wallet) {
        return new WalletResponse()
                .network(wallet.network())
                .address(wallet.address());
    }

    static BankAccountResponse map(Invoice.BankAccount bankAccount) {
        return new BankAccountResponse()
                .bic(bankAccount.bic())
                .accountNumber(bankAccount.accountNumber());
    }

    static InvoicePreviewResponseIndividualBillingProfile map(Invoice.PersonalInfo personalInfo) {
        return new InvoicePreviewResponseIndividualBillingProfile()
                .firstName(personalInfo.firstName())
                .lastName(personalInfo.lastName())
                .address(personalInfo.address());
    }

    static InvoicePreviewResponseCompanyBillingProfile map(Invoice.CompanyInfo companyInfo) {
        return new InvoicePreviewResponseCompanyBillingProfile()
                .name(companyInfo.name())
                .address(companyInfo.address())
                .registrationNumber(companyInfo.registrationNumber())
                .vatRegulationState(map(companyInfo.vatRegulationState()))
                .euVATNumber(companyInfo.euVATNumber())
                ;
    }

    static VatRegulationState map(Invoice.VatRegulationState vatRegulationState) {
        return switch (vatRegulationState) {
            case APPLICABLE -> VatRegulationState.APPLICABLE;
            case REVERSE_CHARGE -> VatRegulationState.REVERSE_CHARGE;
            case NOT_APPLICABLE_NON_UE -> VatRegulationState.NOT_APPLICABLE_NON_UE;
            case NOT_APPLICABLE_FRENCH_NOT_SUBJECT -> VatRegulationState.NOT_APPLICABLE_FRENCH_NOT_SUBJECT;
        };
    }

    static BillingProfileInvoicesPageResponse map(Page<Invoice> page, Integer pageIndex) {
        return new BillingProfileInvoicesPageResponse()
                .invoices(page.getContent().stream().map(BillingProfileMapper::mapToBillingProfileInvoicesPageItemResponse).toList())
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()))
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static BillingProfileInvoicesPageItemResponse mapToBillingProfileInvoicesPageItemResponse(Invoice invoice) {
        return new BillingProfileInvoicesPageItemResponse()
                .id(invoice.id().value())
                .name(invoice.name().value())
                .createdAt(invoice.createdAt())
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
            case DRAFT -> InvoiceStatus.DRAFT;
            case PROCESSING -> InvoiceStatus.PROCESSING;
            case APPROVED -> InvoiceStatus.APPROVED;
            case REJECTED -> InvoiceStatus.REJECTED;
        };
    }

    static ConvertibleMoney toConvertibleMoney(Money money, Money base) {
        return new ConvertibleMoney()
                .amount(money.getValue())
                .currency(map(money.getCurrency()))
                .base(new BaseMoney()
                        .amount(base.getValue())
                        .currency(map(base.getCurrency()))
                        .conversionRate(base.getValue().divide(money.getValue(), 2, RoundingMode.HALF_EVEN))
                );
    }

    static onlydust.com.marketplace.api.contract.model.BillingProfileType map(BillingProfileType type) {
        return switch (type) {
            case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
            case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
        };
    }

    static onlydust.com.marketplace.api.contract.model.BillingProfileType map(onlydust.com.marketplace.accounting.domain.model.BillingProfile.Type type) {
        return switch (type) {
            case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
            case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
        };
    }
}
