package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardShortView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Language;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.ETHEREUM;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BackOfficeMapper {
    static ShortCurrencyResponse toShortCurrency(final Currency currency) {
        return new ShortCurrencyResponse()
                .id(currency.id().value())
                .code(currency.code().toString())
                .name(currency.name())
                .logoUrl(currency.logoUri().orElse(null))
                .decimals(currency.decimals());
    }

    static ShortCurrencyResponse toShortCurrency(final CurrencyView currency) {
        return new ShortCurrencyResponse()
                .id(currency.id().value())
                .code(currency.code())
                .name(currency.name())
                .logoUrl(currency.logoUrl())
                .decimals(currency.decimals());
    }

    static InvoicePage mapInvoicePageToContract(final Page<Invoice> page, final int pageIndex, final String baseUri, final String token) {
        return new InvoicePage()
                .invoices(page.getContent().stream().map(i -> mapInvoice(i, baseUri, token)).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    @SneakyThrows
    static InvoicePageItemResponse mapInvoice(final Invoice invoice, final String baseUri, final String token) {
        return new InvoicePageItemResponse()
                .id(invoice.id().value())
                .status(mapInvoiceStatus(invoice.status()))
                .internalStatus(mapInvoiceInternalStatus(invoice.status()))
                .createdAt(invoice.createdAt())
                .dueAt(invoice.dueAt())
                .amount(invoice.totalAfterTax().getValue())
                .currency(toShortCurrency(invoice.totalAfterTax().getCurrency()))
                .rewardIds(invoice.rewards().stream().map(Invoice.Reward::id).map(UuidWrapper::value).toList())
                .downloadUrl(URI.create("%s/bo/v1/external/invoices/%s?token=%s".formatted(baseUri, invoice.id().value(), token)));
    }

    static InvoicePageV2 mapInvoicePageV2ToContract(final Page<Invoice> page, final int pageIndex) {
        return new InvoicePageV2()
                .invoices(page.getContent().stream().map(BackOfficeMapper::mapInvoiceV2).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    @SneakyThrows
    static InvoicePageItemV2 mapInvoiceV2(final Invoice invoice) {
        return new InvoicePageItemV2()
                .id(invoice.id().value())
                .status(mapInvoiceInternalStatus(invoice.status()))
                .createdAt(invoice.createdAt())
                .billingProfile(mapToLinkResponse(invoice.billingProfileSnapshot()))
                .rewardCount(invoice.rewards().size())
                .totalUsdEquivalent(invoice.totalAfterTax().getValue())
                .totalsPerCurrency(invoice.totals().stream()
                        .map(BackOfficeMapper::totalMoneyViewToResponse)
                        .toList());
    }

    static BillingProfileLinkResponse mapToLinkResponse(Invoice.BillingProfileSnapshot billingProfileSnapshot) {
        return new BillingProfileLinkResponse()
                .id(billingProfileSnapshot.id().value())
                .type(map(billingProfileSnapshot.type()))
                .subject(billingProfileSnapshot.subject());
    }

    static KybResponse mapKyb(Invoice.BillingProfileSnapshot.KybSnapshot kybSnapshot) {
        return new KybResponse()
                .name(kybSnapshot.name())
                .registrationNumber(kybSnapshot.registrationNumber())
                .address(kybSnapshot.address())
                .country(kybSnapshot.countryName())
                .countryCode(kybSnapshot.countryCode())
                .usEntity(kybSnapshot.usEntity())
                .subjectToEuropeVAT(kybSnapshot.subjectToEuVAT())
                .euVATNumber(kybSnapshot.euVATNumber());
    }

    static KycResponse mapKyc(Invoice.BillingProfileSnapshot.KycSnapshot kycSnapshot) {
        return new KycResponse()
                .firstName(kycSnapshot.firstName())
                .lastName(kycSnapshot.lastName())
                .address(kycSnapshot.address())
                .country(kycSnapshot.countryName())
                .countryCode(kycSnapshot.countryCode())
                .usCitizen(kycSnapshot.usCitizen());
    }

    @SneakyThrows
    static InvoiceDetailsResponse mapInvoiceToContract(final InvoiceView invoice, AuthenticatedUser authenticatedUser) {
        return new InvoiceDetailsResponse()
                .id(invoice.id().value())
                .number(invoice.number().toString())
                .status(mapInvoiceInternalStatus(invoice.status()))
                .billingProfile(mapToShortResponse(invoice.billingProfileSnapshot()))
                .rejectionReason(invoice.rejectionReason())
                .createdAt(invoice.createdAt())
                .createdBy(map(invoice.createdBy()))
                .totalEquivalent(new MoneyResponse()
                        .amount(invoice.totalAfterTax().getValue())
                        .currency(toShortCurrency(invoice.totalAfterTax().getCurrency()))
                )
                .rewards(invoice.rewards().stream().map(r -> mapToShortResponse(r, authenticatedUser)).toList());
    }

    static ShortRewardResponse mapToShortResponse(RewardShortView reward, AuthenticatedUser authenticatedUser) {
        return new ShortRewardResponse()
                .id(reward.id().value())
                .status(map(reward.status().as(authenticatedUser)))
                .project(new ProjectLinkResponse()
                        .id(reward.project().id().value())
                        .slug(reward.project().slug())
                        .name(reward.project().name())
                        .logoUrl(reward.project().logoUrl())
                )
                .money(moneyViewToResponse(reward.money()));
    }

    static BillingProfileShortResponse mapToShortResponse(Invoice.BillingProfileSnapshot billingProfileSnapshot) {
        return new BillingProfileShortResponse()
                .id(billingProfileSnapshot.id().value())
                .type(map(billingProfileSnapshot.type()))
                .subject(billingProfileSnapshot.subject())
                .verificationStatus(VerificationStatus.VERIFIED)
                .kyc(billingProfileSnapshot.kyc().map(BackOfficeMapper::mapKyc).orElse(null))
                .kyb(billingProfileSnapshot.kyb().map(BackOfficeMapper::mapKyb).orElse(null));
    }

    static UserResponse map(onlydust.com.marketplace.accounting.domain.view.UserView userView) {
        return new UserResponse()
                .id(userView.id().value())
                .githubUserId(userView.githubUserId())
                .githubLogin(userView.githubLogin())
                .githubAvatarUrl(URI.create(userView.githubAvatarUrl().toString()))
                .email(userView.email())
                .name(userView.name());
    }

    static ProjectCategoryResponse map(ProjectCategory projectCategory) {
        return new ProjectCategoryResponse()
                .id(projectCategory.id().value())
                .name(projectCategory.name())
                .description(projectCategory.description())
                .iconSlug(projectCategory.iconSlug());
    }

    static RewardDetailsResponse map(RewardDetailsView view, AuthenticatedUser authenticatedUser) {
        final var response = new RewardDetailsResponse()
                .id(view.id().value())
                .paymentId(view.paymentId() == null ? null : view.paymentId().value())
                .githubUrls(view.githubUrls())
                .processedAt(view.processedAt())
                .requestedAt(view.requestedAt())
                .money(moneyViewToResponse(view.money())
                )
                .status(BackOfficeMapper.map(view.status().as(authenticatedUser)))
                .project(new ProjectLinkResponse()
                        .id(view.project().id().value())
                        .slug(view.project().slug())
                        .name(view.project().name())
                        .logoUrl(view.project().logoUrl()))
                .sponsors(view.sponsors().stream()
                        .map(shortSponsorView -> new SponsorLinkResponse()
                                .id(shortSponsorView.id().value())
                                .name(shortSponsorView.name())
                                .avatarUrl(shortSponsorView.logoUrl().toString()))
                        .toList())
                .billingProfile(mapToLinkResponse(view.billingProfile()))
                .invoiceId(view.invoice() == null ? null : view.invoice().id().value())
                .receipts(view.receipts().stream().map(BackOfficeMapper::mapReceipt).toList())
                .pendingPayments(new ArrayList<>())
                .paidNotificationDate(view.paidNotificationSentAt());

        if (view.invoice() != null && view.pendingPayments() != null)
            view.pendingPayments().forEach((network, amount) -> response.addPendingPaymentsItem(
                    new PendingPaymentSummaryResponse()
                            .amount(amount.getValue())
                            .network(mapNetwork(network))
                            .billingAccountNumber(view.invoice().billingProfileSnapshot().wallet(network).map(Wallet::address).orElse(null))
            ));

        return response;
    }

    static BillingProfileLinkResponse mapToLinkResponse(BillingProfile billingProfile) {
        return billingProfile == null ? null : new BillingProfileLinkResponse()
                .id(billingProfile.id().value())
                .type(map(billingProfile.type()))
                .subject(billingProfile.subject());
    }

    static TransactionReceipt mapReceipt(Receipt receipt) {
        return new TransactionReceipt()
                .id(receipt.id().value())
                .network(mapNetwork(receipt.network()))
                .reference(receipt.reference())
                .thirdPartyName(receipt.thirdPartyName())
                .thirdPartyAccountNumber(receipt.thirdPartyAccountNumber());
    }

    static MoneyWithUsdEquivalentResponse moneyViewToResponse(final MoneyView view) {
        if (view == null) {
            return null;
        }
        return new MoneyWithUsdEquivalentResponse()
                .amount(view.amount())
                .currency(toShortCurrency(view.currency()))
                .conversionRate(view.usdConversionRate().orElse(null))
                .dollarsEquivalent(view.dollarsEquivalent().orElse(null));
    }

    static TotalMoneyWithUsdEquivalentResponse totalMoneyViewToResponse(final TotalMoneyView view) {
        if (view == null) {
            return null;
        }
        return new TotalMoneyWithUsdEquivalentResponse()
                .amount(view.amount())
                .currency(toShortCurrency(view.currency()))
                .dollarsEquivalent(view.dollarsEquivalent());
    }

    static InvoiceStatus mapInvoiceStatus(final Invoice.Status status) {
        return switch (status) {
            case DRAFT -> throw internalServerError("Unknown invoice status: %s".formatted(status));
            case TO_REVIEW, APPROVED -> InvoiceStatus.PROCESSING;
            case PAID -> InvoiceStatus.COMPLETE;
            case REJECTED -> InvoiceStatus.REJECTED;
        };
    }

    static InvoiceInternalStatus mapInvoiceInternalStatus(final Invoice.Status status) {
        return switch (status) {
            case DRAFT -> throw internalServerError("Unknown invoice status: %s".formatted(status));
            case TO_REVIEW -> InvoiceInternalStatus.TO_REVIEW;
            case APPROVED -> InvoiceInternalStatus.APPROVED;
            case PAID -> InvoiceInternalStatus.PAID;
            case REJECTED -> InvoiceInternalStatus.REJECTED;
        };
    }

    static Invoice.Status mapInvoiceStatus(final UpdateInvoiceStatusRequest.StatusEnum status) {
        return switch (status) {
            case APPROVED -> Invoice.Status.APPROVED;
            case REJECTED -> Invoice.Status.REJECTED;
        };
    }

    static Invoice.Status mapInvoiceStatus(final InvoiceInternalStatus status) {
        return switch (status) {
            case TO_REVIEW -> Invoice.Status.TO_REVIEW;
            case APPROVED -> Invoice.Status.APPROVED;
            case REJECTED -> Invoice.Status.REJECTED;
            case PAID -> Invoice.Status.PAID;
        };
    }

    static ProjectPage mapProjectPageToContract(final Page<ProjectView> projectViewPage, int pageIndex) {
        return new ProjectPage()
                .projects(projectViewPage.getContent().stream().map(project -> new ProjectPageItemResponse()
                        .id(project.getId())
                        .name(project.getName())
                        .logoUrl(project.getLogoUrl())
                ).toList())
                .totalPageNumber(projectViewPage.getTotalPageNumber())
                .totalItemNumber(projectViewPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, projectViewPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, projectViewPage.getTotalPageNumber()));
    }

    static Blockchain mapBlockchain(BlockchainContract blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> ETHEREUM;
            case STARKNET -> Blockchain.STARKNET;
            case OPTIMISM -> Blockchain.OPTIMISM;
            case APTOS -> Blockchain.APTOS;
            case STELLAR -> Blockchain.STELLAR;
        };
    }

    static BlockchainContract mapBlockchain(Blockchain blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> BlockchainContract.ETHEREUM;
            case STARKNET -> BlockchainContract.STARKNET;
            case OPTIMISM -> BlockchainContract.OPTIMISM;
            case APTOS -> BlockchainContract.APTOS;
            case STELLAR -> BlockchainContract.STELLAR;
        };
    }

    static CurrencyResponse mapCurrencyResponse(Currency currency) {
        return new CurrencyResponse()
                .id(currency.id().value())
                .type(mapCurrencyType(currency.type()))
                .name(currency.name())
                .code(currency.code().toString())
                .logoUrl(currency.logoUri().orElse(null))
                .decimals(currency.decimals())
                .description(currency.description().orElse(null))
                .tokens(currency.erc20().stream().map(BackOfficeMapper::mapToken).toList())
                .supportedOn(currency.supportedNetworks().stream().map(BackOfficeMapper::mapNetwork).toList())
                .countryRestrictions(currency.countryRestrictions().stream().map(Country::iso3Code).sorted().toList())
                ;
    }

    @NonNull
    private static Token mapToken(ERC20 token) {
        return new Token()
                .blockchain(mapBlockchain(token.getBlockchain()))
                .address(token.getAddress().toString())
                .name(token.getName())
                .symbol(token.getSymbol())
                .decimals(token.getDecimals());
    }

    static CurrencyType mapCurrencyType(final @NonNull Currency.Type type) {
        return switch (type) {
            case FIAT -> CurrencyType.FIAT;
            case CRYPTO -> CurrencyType.CRYPTO;
        };
    }

    static Network mapTransactionNetwork(final @NonNull TransactionNetwork network) {
        return switch (network) {
            case ETHEREUM -> Network.ETHEREUM;
            case OPTIMISM -> Network.OPTIMISM;
            case STARKNET -> Network.STARKNET;
            case APTOS -> Network.APTOS;
            case STELLAR -> Network.STELLAR;
            case SEPA -> Network.SEPA;
        };
    }

    static TransactionNetwork mapNetwork(final @NonNull Network network) {
        return switch (network) {
            case ETHEREUM -> TransactionNetwork.ETHEREUM;
            case OPTIMISM -> TransactionNetwork.OPTIMISM;
            case STARKNET -> TransactionNetwork.STARKNET;
            case APTOS -> TransactionNetwork.APTOS;
            case STELLAR -> TransactionNetwork.STELLAR;
            case SEPA -> TransactionNetwork.SEPA;
        };
    }

    static RewardStatus.Input map(RewardStatusContract rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_SIGNUP -> RewardStatus.Input.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> RewardStatus.Input.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatus.Input.PENDING_VERIFICATION;
            case GEO_BLOCKED -> RewardStatus.Input.GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> RewardStatus.Input.INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> RewardStatus.Input.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatus.Input.LOCKED;
            case PENDING_REQUEST -> RewardStatus.Input.PENDING_REQUEST;
            case PROCESSING -> RewardStatus.Input.PROCESSING;
            case COMPLETE -> RewardStatus.Input.COMPLETE;
        };
    }

    static RewardStatusContract map(RewardStatus.Output rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_SIGNUP -> RewardStatusContract.PENDING_SIGNUP;
            case PENDING_CONTRIBUTOR -> null;
            case PENDING_BILLING_PROFILE -> RewardStatusContract.PENDING_BILLING_PROFILE;
            case PENDING_COMPANY -> null;
            case PENDING_VERIFICATION -> RewardStatusContract.PENDING_VERIFICATION;
            case GEO_BLOCKED -> RewardStatusContract.GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> RewardStatusContract.INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> RewardStatusContract.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatusContract.LOCKED;
            case PENDING_REQUEST -> RewardStatusContract.PENDING_REQUEST;
            case PROCESSING -> RewardStatusContract.PROCESSING;
            case COMPLETE -> RewardStatusContract.COMPLETE;
        };
    }

    static BillingProfilePayoutInfoResponseBankAccount map(BankAccount bankAccount) {
        return new BillingProfilePayoutInfoResponseBankAccount()
                .number(bankAccount.accountNumber())
                .bic(bankAccount.bic())
                ;
    }

    static KycResponse map(Kyc kyc) {
        return new KycResponse()
                .firstName(kyc.getFirstName())
                .lastName(kyc.getLastName())
                .birthdate(kyc.getBirthdate() == null ? null : kyc.getBirthdate().toInstant().atZone(ZoneOffset.UTC))
                .address(kyc.getAddress())
                .country(kyc.getCountry().map(c -> c.display().orElse(c.iso3Code())).orElse(null))
                .countryCode(kyc.getCountry().map(Country::iso3Code).orElse(null))
                .usCitizen(kyc.isUsCitizen())
                .idDocumentType(kyc.getIdDocumentType() == null ? null : map(kyc.getIdDocumentType()))
                .idDocumentNumber(kyc.getIdDocumentNumber())
                .validUntil(kyc.getValidUntil() == null ? null : kyc.getValidUntil().toInstant().atZone(ZoneOffset.UTC))
                .idDocumentCountryCode(kyc.getIdDocumentCountry().map(Country::iso3Code).orElse(null))
                .sumsubUrl(kyc.sumsubUrl())
                ;
    }

    static KycIdDocumentType map(Kyc.IdDocumentTypeEnum idDocumentType) {
        return switch (idDocumentType) {
            case PASSPORT -> KycIdDocumentType.PASSPORT;
            case ID_CARD -> KycIdDocumentType.ID_CARD;
            case RESIDENCE_PERMIT -> KycIdDocumentType.RESIDENCE_PERMIT;
            case DRIVER_LICENSE -> KycIdDocumentType.DRIVER_LICENSE;
        };
    }

    static KybResponse map(Kyb kyb) {
        return new KybResponse()
                .name(kyb.getName())
                .registrationNumber(kyb.getRegistrationNumber())
                .registrationDate(kyb.getRegistrationDate() == null ? null : kyb.getRegistrationDate().toInstant().atZone(ZoneOffset.UTC))
                .address(kyb.getAddress())
                .country(kyb.getCountry() == null ? null : kyb.getCountry().display().orElse(kyb.getCountry().iso3Code()))
                .countryCode(kyb.getCountry() == null ? null : kyb.getCountry().iso3Code())
                .usEntity(kyb.getUsEntity())
                .subjectToEuropeVAT(kyb.getSubjectToEuropeVAT())
                .euVATNumber(kyb.getEuVATNumber())
                .sumsubUrl(kyb.sumsubUrl())
                ;
    }

    static VerificationStatus map(onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus status) {
        return isNull(status) ? null : switch (status) {
            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
            case STARTED -> VerificationStatus.STARTED;
            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
            case VERIFIED -> VerificationStatus.VERIFIED;
            case REJECTED -> VerificationStatus.REJECTED;
            case CLOSED -> VerificationStatus.CLOSED;
        };
    }

    static BillingProfileType map(BillingProfile.Type type) {
        return switch (type) {
            case COMPANY -> BillingProfileType.COMPANY;
            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
            case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
        };
    }

    static BillingProfile.Type map(BillingProfileType billingProfileType) {
        return switch (billingProfileType) {
            case COMPANY -> BillingProfile.Type.COMPANY;
            case INDIVIDUAL -> BillingProfile.Type.INDIVIDUAL;
            case SELF_EMPLOYED -> BillingProfile.Type.SELF_EMPLOYED;
        };
    }

    static LanguageResponse mapLanguageResponse(Language language) {
        return new LanguageResponse()
                .id(language.id().value())
                .slug(language.slug())
                .name(language.name())
                .fileExtensions(language.fileExtensions().stream().toList())
                .logoUrl(language.logoUrl())
                .bannerUrl(language.bannerUrl());
    }

    static Language mapLanguageUpdateRequest(UUID languageId, LanguageUpdateRequest languageUpdateRequest) {
        return new Language(Language.Id.of(languageId),
                languageUpdateRequest.getName(),
                languageUpdateRequest.getSlug(),
                new HashSet<>(languageUpdateRequest.getFileExtensions()),
                languageUpdateRequest.getLogoUrl(),
                languageUpdateRequest.getBannerUrl());
    }

    static LanguageExtensionListResponse mapLanguageExtensionResponse(Map<String, Optional<Language>> extensions) {
        return new LanguageExtensionListResponse()
                .knownExtensions(extensions.entrySet().stream()
                        .map(entry -> new LanguageExtensionResponse()
                                .extension(entry.getKey())
                                .language(entry.getValue().map(BackOfficeMapper::mapLanguageResponse).orElse(null))
                        ).toList());
    }
}
