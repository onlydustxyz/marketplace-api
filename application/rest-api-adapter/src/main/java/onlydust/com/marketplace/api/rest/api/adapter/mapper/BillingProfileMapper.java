package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileRewardView;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper.toMoney;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BillingProfileMapper {

    static InvoicePreviewResponse map(Invoice preview) {
        return new InvoicePreviewResponse()
                .id(preview.id().value())
                .number(preview.number().value())
                .createdAt(preview.createdAt())
                .dueAt(preview.dueAt())
                .billingProfileType(map(preview.billingProfileType()))
                .individualBillingProfile(preview.billingProfileSnapshot().kyc().map(BillingProfileMapper::map).orElse(null))
                .companyBillingProfile(preview.billingProfileSnapshot().kyb().map(BillingProfileMapper::map).orElse(null))
                .destinationAccounts(new DestinationAccountResponse()
                        .bankAccount(preview.bankAccount().map(BillingProfileMapper::map).orElse(null))
                        .wallets(preview.wallets().stream().map(BillingProfileMapper::map).toList())
                )
                .rewards(preview.rewards().stream().map(BillingProfileMapper::map).toList())
                .totalBeforeTax(map(preview.totalBeforeTax()))
                .taxRate(preview.taxRate())
                .totalTax(map(preview.totalTax()))
                .totalAfterTax(map(preview.totalAfterTax()))
                .totalAfterTaxPerCurrency(preview.totalAfterTaxPerCurrency().stream().map(BillingProfileMapper::map).toList())
                ;
    }

    static InvoiceRewardItemResponse map(Invoice.Reward reward) {
        return new InvoiceRewardItemResponse()
                .id(reward.id().value())
                .date(reward.createdAt())
                .projectName(reward.projectName())
                .amount(MoneyMapper.toConvertibleMoney(reward.amount(), reward.target()))
                ;
    }

    static Invoice.Sort map(String sort) {
        return switch (sort) {
            case "INVOICE_NUMBER" -> Invoice.Sort.NUMBER;
            case "CREATED_AT" -> Invoice.Sort.CREATED_AT;
            case "AMOUNT" -> Invoice.Sort.AMOUNT;
            case "STATUS" -> Invoice.Sort.STATUS;
            default -> throw badRequest("Invalid sort value: %s".formatted(sort));
        };
    }

    static WalletResponse map(Wallet wallet) {
        return new WalletResponse()
                .network(wallet.network().toString())
                .address(wallet.address());
    }

    static BankAccountResponse map(BankAccount bankAccount) {
        return new BankAccountResponse()
                .bic(bankAccount.bic())
                .accountNumber(bankAccount.accountNumber());
    }

    static InvoicePreviewResponseIndividualBillingProfile map(Invoice.BillingProfileSnapshot.KycSnapshot kycSnapshot) {
        return new InvoicePreviewResponseIndividualBillingProfile()
                .firstName(kycSnapshot.firstName())
                .lastName(kycSnapshot.lastName())
                .address(kycSnapshot.address())
                .countryCode(kycSnapshot.countryCode())
                .country(kycSnapshot.countryName())
                ;
    }

    static InvoicePreviewResponseCompanyBillingProfile map(Invoice.BillingProfileSnapshot.KybSnapshot companyInfo) {
        return new InvoicePreviewResponseCompanyBillingProfile()
                .name(companyInfo.name())
                .address(companyInfo.address())
                .registrationNumber(companyInfo.registrationNumber())
                .vatRegulationState(map(companyInfo.vatRegulationState()))
                .euVATNumber(companyInfo.euVATNumber())
                .countryCode(companyInfo.countryCode())
                .country(companyInfo.countryName())
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

    static NewMoney map(Money money) {
        return new NewMoney()
                .amount(money.getValue())
                .currency(mapCurrency(money.getCurrency()));
    }

    static InvoiceStatus map(Invoice.Status status) {
        return switch (status) {
            case DRAFT -> InvoiceStatus.DRAFT;
            case TO_REVIEW, APPROVED -> InvoiceStatus.PROCESSING;
            case PAID -> InvoiceStatus.COMPLETE;
            case REJECTED -> InvoiceStatus.REJECTED;
        };
    }

    static BillingProfileType map(BillingProfile.Type type) {
        return switch (type) {
            case COMPANY -> BillingProfileType.COMPANY;
            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
            case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
        };
    }

    static BillingProfileInvoicesPageResponse map(Page<InvoiceView> page, Integer pageIndex) {
        return new BillingProfileInvoicesPageResponse()
                .invoices(page.getContent().stream().map(BillingProfileMapper::mapToBillingProfileInvoicesPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static BillingProfileInvoicesPageItemResponse mapToBillingProfileInvoicesPageItemResponse(InvoiceView invoice) {
        return new BillingProfileInvoicesPageItemResponse()
                .id(invoice.id().value())
                .number(invoice.number().value())
                .createdAt(invoice.createdAt())
                .totalAfterTax(map(invoice.totalAfterTax()))
                .status(map(invoice.status()));
    }

    static BillingProfileCoworkersPageResponse coworkersPageToResponse(Page<BillingProfileCoworkerView> coworkersPage, int pageIndex) {
        return new BillingProfileCoworkersPageResponse()
                .coworkers(coworkersPage.getContent().stream()
                        .map(BillingProfileMapper::coworkerToResponse)
                        .sorted(Comparator.comparing(BillingProfileCoworkersPageItemResponse::getJoinedAt, nullsLast(naturalOrder()))
                                .thenComparing(BillingProfileCoworkersPageItemResponse::getInvitedAt, nullsLast(naturalOrder())))
                        .toList())
                .totalPageNumber(coworkersPage.getTotalPageNumber())
                .totalItemNumber(coworkersPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, coworkersPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, coworkersPage.getTotalPageNumber()));
    }

    static BillingProfileCoworkersPageItemResponse coworkerToResponse(BillingProfileCoworkerView billingProfileCoworkerView) {
        return new BillingProfileCoworkersPageItemResponse()
                .githubUserId(billingProfileCoworkerView.githubUserId() != null ? billingProfileCoworkerView.githubUserId().value() : null)
                .login(billingProfileCoworkerView.login())
                .avatarUrl(billingProfileCoworkerView.avatarUrl())
                .isRegistered(billingProfileCoworkerView.userId() != null)
                .id(billingProfileCoworkerView.userId() != null ? billingProfileCoworkerView.userId().value() : null)
                .role(mapRole(billingProfileCoworkerView.role()))
                .removable(billingProfileCoworkerView.removable())
                .invitedAt(billingProfileCoworkerView.invitedAt())
                .joinedAt(billingProfileCoworkerView.joinedAt());

    }

    static BillingProfileCoworkerRole mapRole(BillingProfile.User.Role role) {
        return isNull(role) ? null : switch (role) {
            case ADMIN -> BillingProfileCoworkerRole.ADMIN;
            case MEMBER -> BillingProfileCoworkerRole.MEMBER;
        };
    }

    static BillingProfileInvoiceableRewardsResponse mapToInvoiceableRewardsResponse(List<BillingProfileRewardView> invoiceableRewards,
                                                                                    Map<UUID, List<Network>> rewardNetworks,
                                                                                    AuthenticatedUser authenticatedUser) {
        return new BillingProfileInvoiceableRewardsResponse()
                .rewards(invoiceableRewards.stream().map(r -> mapInvoiceableReward(r, rewardNetworks, authenticatedUser)).toList());
    }

    static MyRewardPageItemResponse mapInvoiceableReward(BillingProfileRewardView view,
                                                         Map<UUID, List<Network>> rewardNetworks,
                                                         AuthenticatedUser authenticatedUser) {
        return new MyRewardPageItemResponse()
                .id(view.getId())
                .projectId(view.getProjectId())
                .numberOfRewardedContributions(view.getNumberOfRewardedContributions())
                .rewardedOnProjectLogoUrl(view.getRewardedOnProjectLogoUrl())
                .rewardedOnProjectName(view.getRewardedOnProjectName())
                .amount(toMoney(view.getAmount()))
                .status(RewardMapper.map(view.getStatus().as(authenticatedUser)))
                .requestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()))
                .processedAt(DateMapper.toZoneDateTime(view.getProcessedAt()))
                .unlockDate(DateMapper.toZoneDateTime(view.getUnlockDate()))
                .networks(rewardNetworks.get(view.getId()).stream().map(BillingProfileMapper::map).toList())
                ;
    }

    static NetworkContract map(Network network) {
        return switch (network) {
            case ETHEREUM -> NetworkContract.ETHEREUM;
            case OPTIMISM -> NetworkContract.OPTIMISM;
            case STARKNET -> NetworkContract.STARKNET;
            case APTOS -> NetworkContract.APTOS;
            case STELLAR -> NetworkContract.STELLAR;
            case SEPA -> NetworkContract.SEPA;
        };
    }
}
