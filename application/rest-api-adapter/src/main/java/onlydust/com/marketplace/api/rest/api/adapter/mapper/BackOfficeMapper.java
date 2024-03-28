package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.view.ProjectSponsorView;
import onlydust.com.marketplace.project.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.project.domain.view.backoffice.UserView;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.ETHEREUM;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BackOfficeMapper {

    static AccountResponse mapAccountToResponse(final SponsorAccountStatement accountStatement) {
        final var balance = mapAccountBalance(accountStatement);
        final var account = accountStatement.account();

        return new AccountResponse()
                .id(account.id().value())
                .sponsorId(account.sponsorId().value())
                .lockedUntil(account.lockedUntil().map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .receipts(account.getTransactions().stream().map(transaction -> mapTransactionToReceipt(account, transaction)).toList())
                .currency(balance.getCurrency())
                .initialBalance(balance.getInitialBalance())
                .currentBalance(balance.getCurrentBalance())
                .initialAllowance(balance.getInitialAllowance())
                .currentAllowance(balance.getCurrentAllowance())
                .debt(balance.getDebt())
                .awaitingPaymentAmount(balance.getAwaitingPaymentAmount())
                ;
    }

    static AccountBalance mapAccountBalance(final SponsorAccountStatement accountStatement) {
        final var account = accountStatement.account();
        return new AccountBalance()
                .currency(toShortCurrency(account.currency()))
                .initialBalance(account.initialBalance().getValue())
                .currentBalance(account.balance().getValue())
                .initialAllowance(accountStatement.initialAllowance().getValue())
                .currentAllowance(accountStatement.allowance().getValue())
                .debt(accountStatement.debt().getValue())
                .awaitingPaymentAmount(accountStatement.awaitingPaymentAmount().getValue());
    }

    static SponsorBudgetResponse mapSponsorBudgetResponse(final SponsorAccountStatement accountStatement) {
        final var balance = mapAccountBalance(accountStatement);
        final var account = accountStatement.account();
        return new SponsorBudgetResponse()
                .currency(balance.getCurrency())
                .initialBalance(balance.getInitialBalance())
                .currentBalance(balance.getCurrentBalance())
                .initialAllowance(balance.getInitialAllowance())
                .currentAllowance(balance.getCurrentAllowance())
                .debt(balance.getDebt())
                .awaitingPaymentAmount(balance.getAwaitingPaymentAmount())
                .lockedAmounts(Stream.of(new SponsorBudgetResponseAllOfLockedAmounts()
                                .amount(accountStatement.allowance().getValue())
                                .lockedUntil(account.lockedUntil().map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                        ).filter(l -> l.getLockedUntil() != null)
                        .toList())
                ;
    }

    static TransactionReceipt mapTransactionToReceipt(final SponsorAccount sponsorAccount, final SponsorAccount.Transaction transaction) {
        return new TransactionReceipt()
                .id(transaction.id().value())
                .reference(transaction.reference())
                .network(sponsorAccount.network().map(BackOfficeMapper::mapNetwork).orElse(null))
                .amount(transaction.amount().getValue())
                .thirdPartyName(transaction.thirdPartyName())
                .thirdPartyAccountNumber(transaction.thirdPartyAccountNumber());
    }

    static SponsorAccount.Transaction mapReceiptToTransaction(final TransactionReceipt transaction) {
        return new SponsorAccount.Transaction(
                SponsorAccount.Transaction.Type.DEPOSIT,
                mapTransactionNetwork(transaction.getNetwork()),
                transaction.getReference(),
                Amount.of(transaction.getAmount()),
                transaction.getThirdPartyName(),
                transaction.getThirdPartyAccountNumber());
    }

    static TransactionHistoryPageResponse mapTransactionHistory(final Page<HistoricalTransaction> page, final int pageIndex) {
        return new TransactionHistoryPageResponse()
                .transactions(page.getContent().stream().map(BackOfficeMapper::mapHistoricalTransaction).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static TransactionHistoryPageItemResponse mapHistoricalTransaction(HistoricalTransaction historicalTransaction) {
        return new TransactionHistoryPageItemResponse()
                .date(historicalTransaction.timestamp())
                .type(mapTransactionType(historicalTransaction.type()))
                .network(historicalTransaction.sponsorAccount().network().map(BackOfficeMapper::mapNetwork).orElse(null))
                .lockedUntil(historicalTransaction.sponsorAccount().lockedUntil().map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .project(mapToProjectLink(historicalTransaction.project()))
                .amount(new MoneyWithUsdEquivalentResponse()
                        .amount(historicalTransaction.amount().getValue())
                        .currency(toShortCurrency(historicalTransaction.sponsorAccount().currency()))
                        .dollarsEquivalent(historicalTransaction.usdAmount() == null ? null : historicalTransaction.usdAmount().convertedAmount().getValue())
                        .conversionRate(historicalTransaction.usdAmount() == null ? null : historicalTransaction.usdAmount().conversionRate())
                );
    }

    static HistoricalTransactionType mapTransactionType(HistoricalTransaction.Type type) {
        return switch (type) {
            case DEPOSIT -> HistoricalTransactionType.DEPOSIT;
            case ALLOCATION -> HistoricalTransactionType.ALLOCATION;
        };
    }

    static OldSponsorPage mapSponsorPageToContract(final Page<SponsorView> sponsorPage, int pageIndex) {
        return new OldSponsorPage()
                .sponsors(sponsorPage.getContent().stream().map(sponsor -> new OldSponsorPageItemResponse()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .projectIds(sponsor.projectsWhereSponsorIsActive().stream().map(ProjectSponsorView::projectId).toList())
                ).toList())
                .totalPageNumber(sponsorPage.getTotalPageNumber())
                .totalItemNumber(sponsorPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, sponsorPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, sponsorPage.getTotalPageNumber()));
    }

    static SponsorResponse mapSponsorToResponse(final SponsorView sponsor) {
        return new SponsorResponse()
                .id(sponsor.id())
                .name(sponsor.name())
                .url(sponsor.url())
                .logoUrl(sponsor.logoUrl());
    }

    static SponsorDetailsResponse mapSponsorToDetailsResponse(final SponsorView sponsor, List<SponsorAccountStatement> accountStatements) {
        final var emptyBudget = new SponsorBudgetResponse()
                .initialBalance(BigDecimal.ZERO)
                .currentBalance(BigDecimal.ZERO)
                .initialAllowance(BigDecimal.ZERO)
                .currentAllowance(BigDecimal.ZERO)
                .debt(BigDecimal.ZERO)
                .awaitingPaymentAmount(BigDecimal.ZERO)
                .lockedAmounts(List.of());

        return new SponsorDetailsResponse()
                .id(sponsor.id())
                .name(sponsor.name())
                .url(sponsor.url())
                .logoUrl(sponsor.logoUrl())
                .projects(sponsor.projectsWhereSponsorIsActive().stream().map(p -> mapToProjectWithBudget(p, accountStatements)).toList())
                .availableBudgets(accountStatements.stream()
                        .map(BackOfficeMapper::mapSponsorBudgetResponse)
                        .collect(groupingBy(SponsorBudgetResponse::getCurrency, reducing(emptyBudget, BackOfficeMapper::merge)))
                        .values().stream()
                        .sorted(comparing(b -> b.getCurrency().getCode()))
                        .toList())
                ;
    }

    static SponsorBudgetResponse merge(SponsorBudgetResponse balance1, SponsorBudgetResponse balance2) {
        return new SponsorBudgetResponse()
                .currency(balance2.getCurrency())
                .initialBalance(balance1.getInitialBalance().add(balance2.getInitialBalance()))
                .currentBalance(balance1.getCurrentBalance().add(balance2.getCurrentBalance()))
                .initialAllowance(balance1.getInitialAllowance().add(balance2.getInitialAllowance()))
                .currentAllowance(balance1.getCurrentAllowance().add(balance2.getCurrentAllowance()))
                .debt(balance1.getDebt().add(balance2.getDebt()))
                .awaitingPaymentAmount(balance1.getAwaitingPaymentAmount().add(balance2.getAwaitingPaymentAmount()))
                .lockedAmounts(Stream.concat(balance1.getLockedAmounts().stream(), balance2.getLockedAmounts().stream()).toList())
                ;
    }

    static ProjectWithBudgetResponse mapToProjectWithBudget(final ProjectSponsorView projectSponsorView, List<SponsorAccountStatement> accountStatements) {
        return new ProjectWithBudgetResponse()
                .name(projectSponsorView.projectName())
                .logoUrl(projectSponsorView.projectLogoUrl())
                .remainingBudgets(accountStatements.stream().map(statement -> new MoneyResponse()
                                .amount(statement.amountSentTo(ProjectId.of(projectSponsorView.projectId())).getValue())
                                .currency(toShortCurrency(statement.account().currency()))
                        )
                        .filter(money -> money.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .toList())
                ;
    }

    static ProjectLinkResponse mapToProjectLink(final ShortProjectView project) {
        if (project == null) return null;

        return new ProjectLinkResponse()
                .name(project.name())
                .logoUrl(project.logoUrl())
                ;
    }

    static EcosystemPage mapEcosystemPageToContract(final Page<EcosystemView> ecosystemViewPage, int pageIndex) {
        return new EcosystemPage()
                .ecosystems(ecosystemViewPage.getContent().stream().map(ecosystemView -> new EcosystemPageItemResponse()
                        .id(ecosystemView.getId())
                        .name(ecosystemView.getName())
                        .url(ecosystemView.getUrl())
                        .logoUrl(ecosystemView.getLogoUrl())
                        .projectIds(ecosystemView.getProjectIds())
                ).toList())
                .totalPageNumber(ecosystemViewPage.getTotalPageNumber())
                .totalItemNumber(ecosystemViewPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, ecosystemViewPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, ecosystemViewPage.getTotalPageNumber()));
    }


    static GithubRepositoryPage mapGithubRepositoryPageToResponse(Page<ProjectRepositoryView> projectRepositoryViewPage,
                                                                  int sanitizedPageIndex) {
        final GithubRepositoryPage githubRepositoryPage = new GithubRepositoryPage();
        for (ProjectRepositoryView projectRepositoryView : projectRepositoryViewPage.getContent()) {
            githubRepositoryPage.addGithubRepositoriesItem(new GithubRepositoryResponse()
                    .id(projectRepositoryView.getId())
                    .projectId(projectRepositoryView.getProjectId())
                    .owner(projectRepositoryView.getOwner())
                    .technologies(projectRepositoryView.getTechnologies())
                    .name(projectRepositoryView.getName()));
        }
        githubRepositoryPage.setNextPageIndex(nextPageIndex(sanitizedPageIndex,
                projectRepositoryViewPage.getTotalPageNumber()));
        githubRepositoryPage.setTotalPageNumber(projectRepositoryViewPage.getTotalPageNumber());
        githubRepositoryPage.setTotalItemNumber(projectRepositoryViewPage.getTotalItemNumber());
        githubRepositoryPage.setHasMore(hasMore(sanitizedPageIndex,
                projectRepositoryViewPage.getTotalPageNumber()));
        return githubRepositoryPage;
    }

    static ShortCurrencyResponse toShortCurrency(final Currency currency) {
        return new ShortCurrencyResponse()
                .id(currency.id().value())
                .code(currency.code().toString())
                .name(currency.name())
                .logoUrl(currency.logoUri().orElse(null))
                .decimals(currency.decimals());
    }

    static ProjectLeadInvitationPage mapProjectLeadInvitationPageToContract(final Page<ProjectLeadInvitationView> projectLeadInvitationViewPage,
                                                                            int sanitizedPageIndex) {
        final ProjectLeadInvitationPage projectLeadInvitationPage = new ProjectLeadInvitationPage();
        for (ProjectLeadInvitationView view : projectLeadInvitationViewPage.getContent()) {
            projectLeadInvitationPage.addProjectLeadInvitationsItem(new ProjectLeadInvitationResponse()
                    .id(view.getId())
                    .projectId(view.getProjectId())
                    .githubUserId(view.getGithubUserId()));
        }
        projectLeadInvitationPage.setNextPageIndex(nextPageIndex(sanitizedPageIndex,
                projectLeadInvitationViewPage.getTotalPageNumber()));
        projectLeadInvitationPage.setTotalPageNumber(projectLeadInvitationViewPage.getTotalPageNumber());
        projectLeadInvitationPage.setTotalItemNumber(projectLeadInvitationViewPage.getTotalItemNumber());
        projectLeadInvitationPage.setHasMore(hasMore(sanitizedPageIndex,
                projectLeadInvitationViewPage.getTotalPageNumber()));
        return projectLeadInvitationPage;
    }

    static UserPage mapUserPageToContract(final Page<UserView> userPage, int pageIndex) {
        return new UserPage()
                .users(userPage.getContent().stream().map(user -> new UserPageItemResponse()
                        .id(user.getId())
                        .isCompany(user.getIsCompany())
                        .companyName(user.getCompanyName())
                        .companyNum(user.getCompanyNum())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .address(user.getAddress())
                        .country(user.getCountry())
                        .telegram(user.getTelegram())
                        .twitter(user.getTwitter())
                        .discord(user.getDiscord())
                        .linkedin(user.getLinkedIn())
                        .whatsapp(user.getWhatsApp())
                        .bic(user.getBic())
                        .iban(user.getIban())
                        .ens(user.getEns())
                        .ethAddress(user.getEthAddress())
                        .optimismAddress(user.getOptimismAddress())
                        .starknetAddress(user.getStarknetAddress())
                        .aptosAddress(user.getAptosAddress())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .lastSeenAt(user.getLastSeenAt())
                        .email(user.getEmail())
                        .githubUserId(user.getGithubUserId())
                        .githubLogin(user.getGithubLogin())
                        .githubHtmlUrl(user.getGithubHtmlUrl())
                        .githubAvatarUrl(user.getGithubAvatarUrl())
                        .bio(user.getBio())
                        .location(user.getLocation())
                        .website(user.getWebsite())
                        .lookingForAJob(user.getLookingForAJob())
                        .weeklyAllocatedTime(user.getWeeklyAllocatedTime())
                        .languages(user.getLanguages())
                        .tcAcceptedAt(user.getTcAcceptedAt())
                        .onboardingCompletedAt(user.getOnboardingCompletedAt())
                        .usEntity(user.getUsEntity())
                        .verificationStatus(VerificationStatus.valueOf(user.getVerificationStatus()))
                ).toList())
                .totalPageNumber(userPage.getTotalPageNumber())
                .totalItemNumber(userPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, userPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, userPage.getTotalPageNumber()));
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
                .totalsPerCurrency(invoice.rewards().stream().map(reward ->
                        new TotalMoneyWithUsdEquivalentResponse()
                                .amount(reward.amount().getValue())
                                .currency(toShortCurrency(reward.amount().getCurrency()))
                                .dollarsEquivalent(reward.target().getValue())
                ).toList());
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
    static InvoiceDetailsResponse mapInvoiceToContract(final InvoiceView invoice) {
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
                .rewards(invoice.rewards().stream().map(BackOfficeMapper::mapToShortResponse).toList());
    }

    static ShortRewardResponse mapToShortResponse(Invoice.Reward reward) {
        return new ShortRewardResponse()
                .id(reward.id().value())
//                .status(map(reward.status().asBackofficeUser()))// TODO
                .project(new ProjectLinkResponse()
                                .name(reward.projectName())
//                        .logoUrl(reward.project().logoUrl()) // TODO
                )
                .money(moneyToResponse(reward.amount()));
    }

    static MoneyWithUsdEquivalentResponse moneyToResponse(Money amount) {
        return new MoneyWithUsdEquivalentResponse()
                .amount(amount.getValue())
                .currency(toShortCurrency(amount.getCurrency()))
//                .dollarsEquivalent(amount.().orElse(null) // TODO
                ;
    }

    static ShortRewardResponse mapToShortResponse(RewardDetailsView reward) {
        return new ShortRewardResponse()
                .id(reward.id().value())
                .status(map(reward.status().asBackofficeUser()))
                .project(new ProjectLinkResponse()
                        .name(reward.project().name())
                        .logoUrl(reward.project().logoUrl()))
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

    static UserResponse map(UserView user) {
        return new UserResponse()
                .githubUserId(user.getGithubUserId())
                .githubLogin(user.getGithubLogin())
                .githubAvatarUrl(URI.create(user.getGithubAvatarUrl()))
                .email(user.getEmail())
                .id(user.getId())
                .name(user.getFirstname() + " " + user.getLastname());
    }

    private static BillingProfileResponse map(Invoice.BillingProfileSnapshot billingProfileSnapshot) {
        return new BillingProfileResponse()
                .id(billingProfileSnapshot.id().value())
                .type(switch (billingProfileSnapshot.type()) {
                    case COMPANY -> BillingProfileType.COMPANY;
                    case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                    case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
                })
                .subject(billingProfileSnapshot.subject())
                .kyc(billingProfileSnapshot.kyc().map(BackOfficeMapper::mapKyc).orElse(null))
                .kyb(billingProfileSnapshot.kyb().map(BackOfficeMapper::mapKyb).orElse(null));
    }

    static List<TotalMoneyWithUsdEquivalentResponse> mapNetworkRewardTotals(final List<RewardDetailsView> rewards) {
        return rewards.stream().collect(groupingBy(r -> r.money().currency()))
                .entrySet().stream()
                .map(e -> {
                            final var currency = e.getKey();
                            final var total = e.getValue().stream().map(r -> r.money().amount()).reduce(BigDecimal::add)
                                    .orElseThrow(() -> internalServerError("No reward found for currency %s".formatted(e.getKey())));
                            final var totalUsdEquivalent = e.getValue().stream()
                                    .map(r -> r.money().dollarsEquivalent().orElse(BigDecimal.ZERO))
                                    .reduce(BigDecimal::add)
                                    .orElseThrow(() -> internalServerError("No reward found for currency %s".formatted(e.getKey())));
                            return totalMoneyViewToResponse(new TotalMoneyView(total, currency, totalUsdEquivalent));
                        }
                )
                .sorted(comparing(r -> r.getCurrency().getCode()))
                .toList();
    }

    static RewardDetailsResponse map(RewardDetailsView view) {
        final var response = new RewardDetailsResponse()
                .id(view.id().value())
                .paymentId(view.paymentId() == null ? null : view.paymentId().value())
                .githubUrls(view.githubUrls())
                .processedAt(view.processedAt())
                .requestedAt(view.requestedAt())
                .money(moneyViewToResponse(view.money())
                )
                .status(BackOfficeMapper.map(view.status().asBackofficeUser()))
                .project(new ProjectLinkResponse()
                        .name(view.project().name())
                        .logoUrl(view.project().logoUrl()))
                .sponsors(view.sponsors().stream()
                        .map(shortSponsorView -> new SponsorLinkResponse()
                                .name(shortSponsorView.name())
                                .avatarUrl(shortSponsorView.logoUrl()))
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

    static RewardPageResponse rewardPageToResponse(int pageIndex, Page<RewardDetailsView> page) {
        final RewardPageResponse response = new RewardPageResponse();
        response.setTotalPageNumber(page.getTotalPageNumber());
        response.setTotalItemNumber(page.getTotalItemNumber());
        response.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        response.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        page.getContent().forEach(rewardDetailsView -> response.addRewardsItem(new RewardPageItemResponse()
                .id(rewardDetailsView.id().value())
                .status(BackOfficeMapper.map(rewardDetailsView.status().asBackofficeUser()))
                .requestedAt(rewardDetailsView.requestedAt())
                .project(new ProjectLinkResponse()
                        .name(rewardDetailsView.project().name())
                        .logoUrl(rewardDetailsView.project().logoUrl()))
                .money(moneyViewToResponse(rewardDetailsView.money()))
                .billingProfile(mapToLinkResponse(rewardDetailsView.billingProfile()))
                .invoice(rewardDetailsView.invoice() != null ?
                        new InvoiceLinkResponse()
                                .id(rewardDetailsView.invoice().id().value())
                                .number(rewardDetailsView.invoice().number().toString())
                                .status(mapInvoiceInternalStatus(rewardDetailsView.invoice().status()))
                        : null
                )
                .recipient(rewardDetailsView.recipient() != null ?
                        new RecipientLinkResponse()
                                .login(rewardDetailsView.recipient().login())
                                .avatarUrl(rewardDetailsView.recipient().avatarUrl())
                        : null)
        ));
        return response;
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

    static OldProjectPage mapOldProjectPageToContract(final Page<OldProjectView> projectViewPage, int pageIndex) {
        return new OldProjectPage()
                .projects(projectViewPage.getContent().stream().map(project -> new OldProjectPageItemResponse()
                        .id(project.getId())
                        .name(project.getName())
                        .shortDescription(project.getShortDescription())
                        .longDescription(project.getLongDescription())
                        .moreInfoLinks(project.getMoreInfoLinks())
                        .logoUrl(project.getLogoUrl())
                        .hiring(project.getHiring())
                        .rank(project.getRank())
                        .visibility(mapProjectVisibility(project.getVisibility()))
                        .projectLeads(project.getProjectLeadIds())
                        .createdAt(project.getCreatedAt())
                        .activeContributors(project.getActiveContributors())
                        .newContributors(project.getNewContributors())
                        .uniqueRewardedContributors(project.getUniqueRewardedContributors())
                        .openedIssues(project.getOpenedIssues())
                        .contributions(project.getContributions())
                        .dollarsEquivalentAmountSent(project.getDollarsEquivalentAmountSent())
                        .strkAmountSent(project.getStrkAmountSent())
                ).toList())
                .totalPageNumber(projectViewPage.getTotalPageNumber())
                .totalItemNumber(projectViewPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, projectViewPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, projectViewPage.getTotalPageNumber()));
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

    static ProjectVisibility mapProjectVisibility(onlydust.com.marketplace.project.domain.model.ProjectVisibility visibility) {
        return switch (visibility) {
            case PUBLIC -> ProjectVisibility.PUBLIC;
            case PRIVATE -> ProjectVisibility.PRIVATE;
        };
    }

    static Blockchain mapBlockchain(BlockchainContract blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> ETHEREUM;
            case STARKNET -> Blockchain.STARKNET;
            case OPTIMISM -> Blockchain.OPTIMISM;
            case APTOS -> Blockchain.APTOS;
        };
    }

    static BlockchainContract mapBlockchain(Blockchain blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> BlockchainContract.ETHEREUM;
            case STARKNET -> BlockchainContract.STARKNET;
            case OPTIMISM -> BlockchainContract.OPTIMISM;
            case APTOS -> BlockchainContract.APTOS;
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

    static CurrencyStandard mapCurrencyStandard(final @NonNull Currency.Standard s) {
        return switch (s) {
            case ERC20 -> CurrencyStandard.ERC20;
            case ISO4217 -> CurrencyStandard.ISO4217;
        };
    }

    static CurrencyType mapCurrencyType(final @NonNull Currency.Type type) {
        return switch (type) {
            case FIAT -> CurrencyType.FIAT;
            case CRYPTO -> CurrencyType.CRYPTO;
        };
    }

    static Ecosystem mapEcosystemToDomain(final EcosystemRequest ecosystemRequest) {
        return Ecosystem.builder()
                .name(ecosystemRequest.getName())
                .url(ecosystemRequest.getUrl())
                .logoUrl(ecosystemRequest.getLogoUrl())
                .build();
    }

    static EcosystemResponse mapEcosystemToResponse(final Ecosystem ecosystem) {
        return new EcosystemResponse()
                .id(ecosystem.getId())
                .url(ecosystem.getUrl())
                .name(ecosystem.getName())
                .logoUrl(ecosystem.getLogoUrl());
    }

    static Network mapTransactionNetwork(final @NonNull TransactionNetwork network) {
        return switch (network) {
            case ETHEREUM -> Network.ETHEREUM;
            case OPTIMISM -> Network.OPTIMISM;
            case STARKNET -> Network.STARKNET;
            case APTOS -> Network.APTOS;
            case SEPA -> Network.SEPA;
        };
    }

    static TransactionNetwork mapNetwork(final @NonNull Network network) {
        return switch (network) {
            case ETHEREUM -> TransactionNetwork.ETHEREUM;
            case OPTIMISM -> TransactionNetwork.OPTIMISM;
            case STARKNET -> TransactionNetwork.STARKNET;
            case APTOS -> TransactionNetwork.APTOS;
            case SEPA -> TransactionNetwork.SEPA;
        };
    }

    static RewardStatus map(RewardStatusContract rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_SIGNUP -> RewardStatus.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> RewardStatus.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatus.PENDING_VERIFICATION;
            case PAYMENT_BLOCKED -> RewardStatus.PAYMENT_BLOCKED;
            case PAYOUT_INFO_MISSING -> RewardStatus.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatus.LOCKED;
            case PENDING_REQUEST -> RewardStatus.PENDING_REQUEST;
            case PROCESSING -> RewardStatus.PROCESSING;
            case COMPLETE -> RewardStatus.COMPLETE;
        };
    }

    static RewardStatusContract map(RewardStatus rewardStatus) {
        return switch (rewardStatus.asBackofficeUser()) {
            case PENDING_SIGNUP -> RewardStatusContract.PENDING_SIGNUP;
            case PENDING_CONTRIBUTOR -> null;
            case PENDING_BILLING_PROFILE -> RewardStatusContract.PENDING_BILLING_PROFILE;
            case PENDING_COMPANY -> null;
            case PENDING_VERIFICATION -> RewardStatusContract.PENDING_VERIFICATION;
            case PAYMENT_BLOCKED -> RewardStatusContract.PAYMENT_BLOCKED;
            case PAYOUT_INFO_MISSING -> RewardStatusContract.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatusContract.LOCKED;
            case PENDING_REQUEST -> RewardStatusContract.PENDING_REQUEST;
            case PROCESSING -> RewardStatusContract.PROCESSING;
            case COMPLETE -> RewardStatusContract.COMPLETE;
        };
    }


    static BillingProfileResponse map(BillingProfileView billingProfile) {
        return new BillingProfileResponse()
                .id(billingProfile.getId().value())
                .subject(billingProfile.subject())
                .type(map(billingProfile.getType()))
                .verificationStatus(map(billingProfile.getVerificationStatus()))
                .kyb(billingProfile.getKyb() == null ? null : map(billingProfile.getKyb()))
                .kyc(billingProfile.getKyc() == null ? null : map(billingProfile.getKyc()))
                .admins(billingProfile.getAdmins().stream().map(BackOfficeMapper::map).toList())
                ;
    }

    static UserResponse map(BillingProfileView.User user) {
        return new UserResponse()
                .id(user.id().value())
                .githubUserId(user.githubUserId().value())
                .githubLogin(user.githubLogin())
                .githubAvatarUrl(user.githubAvatarUrl())
                .email(user.email())
                ;
    }

    static KycResponse map(Kyc kyc) {
        return new KycResponse()
                .firstName(kyc.getFirstName())
                .lastName(kyc.getLastName())
                .birthdate(kyc.getBirthdate() == null ? null : kyc.getBirthdate().toInstant().atZone(ZoneOffset.UTC))
                .address(kyc.getAddress())
                .country(kyc.getCountry() == null ? null : kyc.getCountry().display().orElse(kyc.getCountry().iso3Code()))
                .countryCode(kyc.getCountry() == null ? null : kyc.getCountry().iso3Code())
                .usCitizen(kyc.getUsCitizen())
                .idDocumentType(kyc.getIdDocumentType() == null ? null : map(kyc.getIdDocumentType()))
                .idDocumentNumber(kyc.getIdDocumentNumber())
                .validUntil(kyc.getValidUntil() == null ? null : kyc.getValidUntil().toInstant().atZone(ZoneOffset.UTC))
                .idDocumentCountryCode(kyc.getIdDocumentCountryCode())
                .sumsubUrl(kyc.sumsubUrl())
                ;
    }

    static KycResponse.IdDocumentTypeEnum map(Kyc.IdDocumentTypeEnum idDocumentType) {
        return switch (idDocumentType) {
            case PASSPORT -> KycResponse.IdDocumentTypeEnum.PASSPORT;
            case ID_CARD -> KycResponse.IdDocumentTypeEnum.ID_CARD;
            case RESIDENCE_PERMIT -> KycResponse.IdDocumentTypeEnum.RESIDENCE_PERMIT;
            case DRIVER_LICENSE -> KycResponse.IdDocumentTypeEnum.DRIVER_LICENSE;
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
        return switch (status) {
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
}
