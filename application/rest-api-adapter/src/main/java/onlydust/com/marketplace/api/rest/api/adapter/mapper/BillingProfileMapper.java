package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;

import java.math.RoundingMode;

public interface BillingProfileMapper {

    static InvoicePreviewResponse map(Invoice preview) {
        return new InvoicePreviewResponse()
                .id(preview.id().value())
                .number(preview.number().value())
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

    static NewMoney map(onlydust.com.marketplace.accounting.domain.model.Money money) {
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

    static ConvertibleMoney toConvertibleMoney(onlydust.com.marketplace.accounting.domain.model.Money money, Money base) {
        return new ConvertibleMoney()
                .amount(money.getValue())
                .currency(map(money.getCurrency()))
                .base(new BaseMoney()
                        .amount(base.getValue())
                        .currency(map(base.getCurrency()))
                        .conversionRate(base.getValue().divide(money.getValue(), 2, RoundingMode.HALF_EVEN))
                );
    }

    static onlydust.com.marketplace.api.contract.model.BillingProfileType map(BillingProfile.Type type) {
        return switch (type) {
            case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
            case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
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
                .number(invoice.number().value())
                .createdAt(invoice.createdAt())
                .totalAfterTax(map(invoice.totalAfterTax()))
                .status(map(invoice.status()));
    }
}
