package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.view.ProjectSponsorView;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.SearchRewardMapper.moneyViewToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.SearchRewardMapper.totalMoneyViewToResponse;
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
                .debt(accountStatement.initialAllowance().subtract(account.initialBalance()).getValue())
                .awaitingPaymentAmount(accountStatement.awaitingPaymentAmount().getValue());
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
        final var emptyBalance = new AccountBalance()
                .initialBalance(BigDecimal.ZERO)
                .currentBalance(BigDecimal.ZERO)
                .initialAllowance(BigDecimal.ZERO)
                .currentAllowance(BigDecimal.ZERO)
                .debt(BigDecimal.ZERO)
                .awaitingPaymentAmount(BigDecimal.ZERO);

        return new SponsorDetailsResponse()
                .id(sponsor.id())
                .name(sponsor.name())
                .url(sponsor.url())
                .logoUrl(sponsor.logoUrl())
                .projects(sponsor.projectsWhereSponsorIsActive().stream().map(BackOfficeMapper::mapToProjectLink).toList())
                .availableBudgets(accountStatements.stream()
                        .map(BackOfficeMapper::mapAccountBalance)
                        .collect(groupingBy(AccountBalance::getCurrency, reducing(emptyBalance, BackOfficeMapper::merge)))
                        .values().stream().toList())
                ;
    }

    static AccountBalance merge(AccountBalance balance1, AccountBalance balance2) {
        return new AccountBalance()
                .currency(balance2.getCurrency())
                .initialBalance(balance1.getInitialBalance().add(balance2.getInitialBalance()))
                .currentBalance(balance1.getCurrentBalance().add(balance2.getCurrentBalance()))
                .initialAllowance(balance1.getInitialAllowance().add(balance2.getInitialAllowance()))
                .currentAllowance(balance1.getCurrentAllowance().add(balance2.getCurrentAllowance()))
                .debt(balance1.getDebt().add(balance2.getDebt()))
                .awaitingPaymentAmount(balance1.getAwaitingPaymentAmount().add(balance2.getAwaitingPaymentAmount()))
                ;
    }

    static ProjectLinkResponse mapToProjectLink(final ProjectSponsorView projectSponsorView) {
        return new ProjectLinkResponse()
                .name(projectSponsorView.projectName())
                .logoUrl(projectSponsorView.projectLogoUrl())
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
                .logoUrl(currency.logoUri().orElse(null));
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
                ).toList())
                .totalPageNumber(userPage.getTotalPageNumber())
                .totalItemNumber(userPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, userPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, userPage.getTotalPageNumber()));
    }

    static PaymentPage mapPaymentPageToContract(final Page<PaymentView> paymentPage, int pageIndex) {
        return new PaymentPage()
                .payments(paymentPage.getContent().stream().map(payment -> new PaymentPageItemResponse()
                        .id(payment.getId())
                        .projectId(payment.getProjectId())
                        .amount(payment.getAmount())
                        .currency(switch (payment.getCurrency()) {
                            case STRK -> CurrencyCode.STRK;
                            case USD -> CurrencyCode.USD;
                            case APT -> CurrencyCode.APT;
                            case OP -> CurrencyCode.OP;
                            case ETH -> CurrencyCode.ETH;
                            case LORDS -> CurrencyCode.LORDS;
                            case USDC -> CurrencyCode.USDC;
                        })
                        .recipientId(payment.getRecipientId())
                        .requestorId(payment.getRequestorId())
                        .isPayable(payment.recipientPayoutInfoValid())
                        .payoutSettings(payment.recipientPayoutSettings())
                        .items(payment.getItems())
                        .requestedAt(payment.getRequestedAt())
                        .processedAt(payment.getProcessedAt())
                        .pullRequestsCount(payment.getPullRequestsCount())
                        .issuesCount(payment.getIssuesCount())
                        .dustyIssuesCount(payment.getDustyIssuesCount())
                        .codeReviewsCount(payment.getCodeReviewsCount())
                ).toList())
                .totalPageNumber(paymentPage.getTotalPageNumber())
                .totalItemNumber(paymentPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, paymentPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, paymentPage.getTotalPageNumber()));
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
                .billingProfile(new BillingProfileResponse()
                        .id(invoice.billingProfileSnapshot().id().value())
                        .type(switch (invoice.billingProfileType()) {
                            case COMPANY -> BillingProfileType.COMPANY;
                            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                            case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
                        })
                        .name(invoice.billingProfileSnapshot().subject())
                        .admins(null) //TODO: add admins when implementing the new version for pennylane
                )
                .rewardCount(invoice.rewards().size())
                .totalUsdEquivalent(invoice.totalAfterTax().getValue())
                .totalsPerCurrency(invoice.rewards().stream().map(reward ->
                        new TotalMoneyWithUsdEquivalentResponse()
                                .amount(reward.amount().getValue())
                                .currency(toShortCurrency(reward.amount().getCurrency()))
                                .dollarsEquivalent(reward.target().getValue())
                ).toList());
    }

    @SneakyThrows
    static InvoiceResponse mapInvoiceToContract(final Invoice invoice, List<BillingProfileCoworkerView> billingProfileAdmins,
                                                final List<BackofficeRewardView> rewards) {
        return new InvoiceResponse()
                .id(invoice.id().value())
                .number(invoice.number().toString())
                .rejectionReason(invoice.rejectionReason())
                .status(mapInvoiceInternalStatus(invoice.status()))
                .createdAt(invoice.createdAt())
                .billingProfile(new BillingProfileResponse()
                        .id(invoice.billingProfileSnapshot().id().value())
                        .type(switch (invoice.billingProfileType()) {
                            case COMPANY -> BillingProfileType.COMPANY;
                            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                            case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
                        })
                        .name(invoice.billingProfileSnapshot().subject())
                        .admins(billingProfileAdmins.stream()
                                .map(admin -> new BillingProfileAdminResponse()
                                        .name(admin.login())
                                        .avatarUrl(admin.avatarUrl())
                                        .email(admin.email())
                                ).toList()
                        )
                )
                .totalEquivalent(new MoneyResponse()
                        .amount(invoice.totalAfterTax().getValue())
                        .currency(toShortCurrency(invoice.totalAfterTax().getCurrency()))
                )
                .rewardsPerNetwork(mapInvoiceRewardsPerNetworks(invoice, rewards));
    }

    static List<InvoiceRewardsPerNetwork> mapInvoiceRewardsPerNetworks(final Invoice invoice, final List<BackofficeRewardView> rewards) {
        return rewards.stream().collect(groupingBy(BackofficeRewardView::network))
                .entrySet().stream()
                .map(e -> {
                            final var totalUsdEquivalent = e.getValue().stream()
                                    .map(r -> r.money().dollarsEquivalent().orElse(BigDecimal.ZERO))
                                    .reduce(BigDecimal::add)
                                    .orElseThrow(() -> internalServerError("No reward found for network %s".formatted(e.getKey())));

                            return new InvoiceRewardsPerNetwork()
                                    .network(mapNetwork(e.getKey()))
                                    .billingAccountNumber(invoice.wallets().stream()
                                            .filter(w -> w.network() == e.getKey())
                                            .findFirst()
                                            .map(Wallet::address)
                                            .orElse(null))
                                    .totalUsdEquivalent(totalUsdEquivalent)
                                    .totalsPerCurrency(mapNetworkRewardTotals(e.getValue()))
                                    .rewards(mapNetworkRewards(e.getValue()));
                        }
                )
                .sorted(comparing(InvoiceRewardsPerNetwork::getNetwork))
                .toList();
    }

    static List<InvoiceRewardResponse> mapNetworkRewards(final List<BackofficeRewardView> rewards) {
        return rewards.stream()
                .map(reward -> new InvoiceRewardResponse()
                        .id(reward.id().value())
                        .requestedAt(reward.requestedAt())
                        .processedAt(reward.processedAt())
                        .githubUrls(reward.githubUrls())
                        .project(new ProjectLinkResponse()
                                .name(reward.project().name())
                                .logoUrl(reward.project().logoUrl()))
                        .sponsors(reward.sponsors().stream().map(sponsor ->
                                new SponsorLinkResponse()
                                        .name(sponsor.name())
                                        .avatarUrl(sponsor.logoUrl())
                        ).toList())
                        .money(moneyViewToResponse(reward.money()))
                        .transactionReferences(reward.transactionReferences())
                )
                .sorted(comparing(InvoiceRewardResponse::getRequestedAt))
                .toList();
    }

    static List<TotalMoneyWithUsdEquivalentResponse> mapNetworkRewardTotals(final List<BackofficeRewardView> rewards) {
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

    static CurrencyResponse mapCurrencyResponse(onlydust.com.marketplace.accounting.domain.model.Currency currency) {
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

    static CurrencyStandard mapCurrencyStandard(final @NonNull onlydust.com.marketplace.accounting.domain.model.Currency.Standard s) {
        return switch (s) {
            case ERC20 -> CurrencyStandard.ERC20;
            case ISO4217 -> CurrencyStandard.ISO4217;
        };
    }

    static CurrencyType mapCurrencyType(final @NonNull onlydust.com.marketplace.accounting.domain.model.Currency.Type type) {
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
            case SWIFT -> Network.SWIFT;
        };
    }

    static TransactionNetwork mapNetwork(final @NonNull Network network) {
        return switch (network) {
            case ETHEREUM -> TransactionNetwork.ETHEREUM;
            case OPTIMISM -> TransactionNetwork.OPTIMISM;
            case STARKNET -> TransactionNetwork.STARKNET;
            case APTOS -> TransactionNetwork.APTOS;
            case SEPA -> TransactionNetwork.SEPA;
            case SWIFT -> TransactionNetwork.SWIFT;
        };
    }

    static PendingPaymentResponse mapPendingPaymentToResponse(PayableReward payableReward) {
        return new PendingPaymentResponse()
                .rewardId(payableReward.id().value())
                .amount(payableReward.amount().getValue())
                .currency(mapTransactionalCurrency(payableReward.currency()))
                ;
    }

    static TransactionalCurrencyResponse mapTransactionalCurrency(PayableCurrency currency) {
        return new TransactionalCurrencyResponse()
                .id(currency.id().value())
                .code(currency.code().toString())
                .name(currency.name())
                .logoUrl(currency.logoUrl().orElse(null))
                .type(mapCurrencyType(currency.type()))
                .standard(currency.standard().map(BackOfficeMapper::mapCurrencyStandard).orElse(null))
                .blockchain(currency.blockchain().map(BackOfficeMapper::mapBlockchain).orElse(null))
                .address(currency.address().map(Hash::toString).orElse(null));
    }

    static RewardStatus map(RewardStatusContract rewardStatus) {
        return new RewardStatus(switch (rewardStatus) {
            case PENDING_SIGNUP -> RewardStatus.AsUser.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> RewardStatus.AsUser.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatus.AsUser.PENDING_VERIFICATION;
            case PAYMENT_BLOCKED -> RewardStatus.AsUser.PAYMENT_BLOCKED;
            case PAYOUT_INFO_MISSING -> RewardStatus.AsUser.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatus.AsUser.LOCKED;
            case PENDING_REQUEST -> RewardStatus.AsUser.PENDING_REQUEST;
            case PROCESSING -> RewardStatus.AsUser.PROCESSING;
            case COMPLETE -> RewardStatus.AsUser.COMPLETE;
        });
    }

    static RewardStatusContract map(RewardStatus.AsUser rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_SIGNUP -> RewardStatusContract.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> RewardStatusContract.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatusContract.PENDING_VERIFICATION;
            case PAYMENT_BLOCKED -> RewardStatusContract.PAYMENT_BLOCKED;
            case PAYOUT_INFO_MISSING -> RewardStatusContract.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatusContract.LOCKED;
            case PENDING_REQUEST -> RewardStatusContract.PENDING_REQUEST;
            case PROCESSING -> RewardStatusContract.PROCESSING;
            case COMPLETE -> RewardStatusContract.COMPLETE;
        };
    }
}
