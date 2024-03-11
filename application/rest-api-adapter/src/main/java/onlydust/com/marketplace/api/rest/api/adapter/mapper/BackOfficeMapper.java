package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.RewardStatus;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileAdminView;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.ETHEREUM;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BackOfficeMapper {

    static AccountResponse mapAccountToResponse(final SponsorAccountStatement accountStatement) {
        final var account = accountStatement.account();
        return new AccountResponse()
                .id(account.id().value())
                .sponsorId(account.sponsorId().value())
                .currencyId(account.currency().id().value())
                .lockedUntil(account.lockedUntil().map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .balance(account.balance().getValue())
                .allowance(accountStatement.allowance().getValue())
                .awaitingPaymentAmount(accountStatement.awaitingPaymentAmount().getValue())
                .receipts(account.getTransactions().stream()
                        .map(transaction -> mapTransactionToReceipt(account, transaction)).toList());
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
                mapTransactionNetwork(transaction.getNetwork()),
                transaction.getReference(),
                Amount.of(transaction.getAmount()),
                transaction.getThirdPartyName(),
                transaction.getThirdPartyAccountNumber());
    }


    static SponsorPage mapSponsorPageToContract(final Page<SponsorView> sponsorPage, int pageIndex) {
        return new SponsorPage()
                .sponsors(sponsorPage.getContent().stream().map(sponsor -> new SponsorPageItemResponse()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .projectIds(sponsor.projectIdsWhereSponsorIsActive().stream().toList())
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
                .logoUrl(sponsor.logoUrl())
                .projectIds(sponsor.projectIdsWhereSponsorIsActive().stream().toList());
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

    static BudgetPage mapBudgetPageToResponse(Page<ProjectBudgetView> projectBudgetViewPage, int sanitizedPageIndex) {
        final BudgetPage budgetPage = new BudgetPage();
        for (ProjectBudgetView view : projectBudgetViewPage.getContent()) {
            budgetPage.addBudgetsItem(new BudgetResponse()
                    .initialAmount(view.getInitialAmount())
                    .remainingAmount(view.getRemainingAmount())
                    .spentAmount(view.getSpentAmount())
                    .id(view.getId())
                    .currency(mapCurrency(view.getCurrency()))
                    .projectId(view.getProjectId())
                    .initialAmountDollarsEquivalent(view.getInitialAmountDollarsEquivalent())
                    .remainingAmountDollarsEquivalent(view.getRemainingAmountDollarsEquivalent())
                    .spentAmountDollarsEquivalent(view.getSpentAmountDollarsEquivalent()));
        }
        budgetPage.setNextPageIndex(nextPageIndex(sanitizedPageIndex,
                projectBudgetViewPage.getTotalPageNumber()));
        budgetPage.setTotalPageNumber(projectBudgetViewPage.getTotalPageNumber());
        budgetPage.setTotalItemNumber(projectBudgetViewPage.getTotalItemNumber());
        budgetPage.setHasMore(hasMore(sanitizedPageIndex,
                projectBudgetViewPage.getTotalPageNumber()));
        return budgetPage;
    }

    static CurrencyCode mapCurrency(final Currency currency) {
        return switch (currency) {
            case STRK -> CurrencyCode.STRK;
            case USD -> CurrencyCode.USD;
            case APT -> CurrencyCode.APT;
            case OP -> CurrencyCode.OP;
            case ETH -> CurrencyCode.ETH;
            case LORDS -> CurrencyCode.LORDS;
            case USDC -> CurrencyCode.USDC;
        };
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
                        .verificationStatus(switch (user.getOldVerificationStatus()) {
                            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
                            case STARTED -> VerificationStatus.STARTED;
                            case REJECTED -> VerificationStatus.REJECTED;
                            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
                            case CLOSED -> VerificationStatus.CLOSED;
                            case VERIFIED -> VerificationStatus.VERIFIED;
                        })
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
                        .budgetId(payment.getBudgetId())
                        .projectId(payment.getProjectId())
                        .amount(payment.getAmount())
                        .currency(mapCurrency(payment.getCurrency()))
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
                .currencyId(invoice.totalAfterTax().getCurrency().id().value())
                .rewardIds(invoice.rewards().stream().map(Invoice.Reward::id).map(UuidWrapper::value).toList())
                .downloadUrl(URI.create("%s/bo/v1/external/invoices/%s?token=%s".formatted(baseUri, invoice.id().value(), token)));
    }

    static InvoicePageV2 mapInvoicePageV2ToContract(final Page<Invoice> page, final int pageIndex) {
        return new InvoicePageV2()
                .invoices(page.getContent().stream().map(i -> mapInvoiceV2(i)).toList())
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
                        .id(invoice.billingProfileId().value())
                        .type(invoice.companyInfo().isPresent() ? BillingProfileType.COMPANY : BillingProfileType.INDIVIDUAL)
                        .name(invoice.companyInfo().map(Invoice.CompanyInfo::name).orElse(invoice.personalInfo().map(Invoice.PersonalInfo::fullName).orElse(null)))
                        .admins(null) //TODO: add admins when implementing the new version for pennylane
                )
                .rewardCount(invoice.rewards().size())
                .totalEquivalent(new MoneyLinkResponse()
                        .amount(invoice.totalAfterTax().getValue())
                        .dollarsEquivalent(invoice.totalAfterTax().getValue())
                        .currencyCode(invoice.totalAfterTax().getCurrency().code().toString())
                        .currencyName(invoice.totalAfterTax().getCurrency().name())
                        .currencyLogoUrl(invoice.totalAfterTax().getCurrency().logoUri().map(URI::toString).orElse(null))
                        .conversionRate(null) //TODO: add conversion rate when implementing the new version for pennylane
                )
                .totalPerCurrency(invoice.rewards().stream().map(reward ->
                        new MoneyLinkResponse()
                                .amount(reward.amount().getValue())
                                .dollarsEquivalent(reward.target().getValue())
                                .currencyCode(reward.amount().getCurrency().code().toString())
                                .currencyName(reward.amount().getCurrency().name())
                                .currencyLogoUrl(reward.amount().getCurrency().logoUri().map(URI::toString).orElse(null))
                                .conversionRate(null) //TODO: add conversion rate when implementing the new version for pennylane
                ).toList());
    }

    @SneakyThrows
    static InvoiceResponse mapInvoiceToContract(final Invoice invoice, List<BillingProfileCoworkerView> billingProfileAdmins, final List<RewardView> rewards) {
        ;
        final BillingProfileType type = invoice.companyInfo().isPresent() ? BillingProfileType.COMPANY : BillingProfileType.INDIVIDUAL;
        final BillingProfileResponse billingProfileResponse = new BillingProfileResponse()
                .id(invoice.billingProfileId().value())
                .type(type)
                .name(invoice.companyInfo().map(Invoice.CompanyInfo::name).orElse(invoice.personalInfo().map(Invoice.PersonalInfo::fullName).orElse(null)))
                .admins(billingProfileAdmins.stream()
                        .map(admin -> new BillingProfileAdminResponse()
                                .name(admin.login())
                                .avatarUrl(admin.avatarUrl())
                                .email(admin.email())
                        ).toList()
                );
        switch (type) {
            case INDIVIDUAL -> billingProfileResponse.kyc(mapShortBillingProfileAdminToKyc(rewards.get(0).billingProfileAdmin()));
            case COMPANY -> billingProfileResponse.kyb(mapShortBillingProfileAdminToKyb(rewards.get(0).billingProfileAdmin()));
        }
        return new InvoiceResponse()
                .id(invoice.id().value())
                .number(invoice.number().toString())
                .rejectionReason(invoice.rejectionReason())
                .status(mapInvoiceInternalStatus(invoice.status()))
                .createdAt(invoice.createdAt())
                .billingProfile(billingProfileResponse)
                .totalEquivalent(new MoneyResponse()
                        .amount(invoice.totalAfterTax().getValue())
                        .currencyCode(invoice.totalAfterTax().getCurrency().code().toString())
                        .currencyName(invoice.totalAfterTax().getCurrency().name())
                        .currencyLogoUrl(invoice.totalAfterTax().getCurrency().logoUri().map(URI::toString).orElse(null))
                )
                .rewardsPerNetwork(mapInvoiceRewardsPerNetworks(invoice, rewards));
    }

    static List<InvoiceRewardsPerNetwork> mapInvoiceRewardsPerNetworks(final Invoice invoice, final List<RewardView> rewards) {
        final Map<Network, List<RewardView>> rewardsPerNetworks = rewards.stream().collect(groupingBy(RewardView::network));

        return rewardsPerNetworks.entrySet().stream()
                .map(e -> {
                            final var totalEquivalent = e.getValue().stream().map(r -> r.money().dollarsEquivalent()).reduce(BigDecimal::add)
                                    .orElseThrow(() -> internalServerError("No reward found for network %s".formatted(e.getKey())));

                            return new InvoiceRewardsPerNetwork()
                                    .network(mapNetwork(e.getKey()))
                                    .billingAccountNumber(invoice.wallets().stream()
                                            .filter(w -> w.network() == e.getKey())
                                            .findFirst()
                                            .map(Invoice.Wallet::address)
                                            .orElse(null))
                                    .dollarsEquivalent(totalEquivalent)
                                    .totalPerCurrency(mapNetworkRewardTotals(e.getValue()))
                                    .rewards(mapNetworkRewards(e.getValue()));
                        }
                )
                .sorted(comparing(InvoiceRewardsPerNetwork::getNetwork))
                .toList();
    }

    static List<InvoiceRewardResponse> mapNetworkRewards(final List<RewardView> rewards) {
        final Map<String, List<RewardView>> rewardsPerCurrencyCode = rewards.stream().collect(groupingBy(r -> r.money().currencyCode()));

        return rewards.stream()
                .map(reward -> new InvoiceRewardResponse()
                        .id(reward.id())
                        .requestedAt(reward.requestedAt())
                        .processedAt(reward.processedAt())
                        .githubUrls(reward.githubUrls())
                        .project(new ProjectLinkResponse()
                                .name(reward.projectName())
                                .logoUrl(reward.projectLogoUrl()))
                        .sponsors(reward.sponsors().stream().map(sponsor ->
                                new SponsorLinkResponse()
                                        .name(sponsor.name())
                                        .avatarUrl(sponsor.logoUrl())
                        ).toList())
                        .money(new MoneyLinkResponse()
                                .amount(reward.money().amount())
                                .dollarsEquivalent(reward.money().dollarsEquivalent())
                                .currencyCode(reward.money().currencyCode())
                                .currencyName(reward.money().currencyName())
                                .currencyLogoUrl(reward.money().currencyLogoUrl())
                        )
                        .transactionHash(reward.transactionHash())
                )
                .sorted(comparing(InvoiceRewardResponse::getRequestedAt))
                .toList();
    }

    static List<MoneyLinkResponse> mapNetworkRewardTotals(final List<RewardView> rewards) {
        final Map<String, List<RewardView>> rewardsPerCurrencyCode = rewards.stream().collect(groupingBy(r -> r.money().currencyCode()));

        return rewardsPerCurrencyCode.entrySet().stream()
                .map(e -> {
                            final var currencyCode = e.getKey();
                            final var currencyName = rewards.stream().findFirst().map(r -> r.money().currencyName()).orElse(null);
                            final var currencyLogoUrl = rewards.stream().findFirst().map(r -> r.money().currencyLogoUrl()).orElse(null);
                            final var total = e.getValue().stream().map(r -> r.money().amount()).reduce(BigDecimal::add)
                                    .orElseThrow(() -> internalServerError("No reward found for currency %s".formatted(e.getKey())));
                            final var totalEquivalent = e.getValue().stream().map(r -> r.money().dollarsEquivalent()).reduce(BigDecimal::add)
                                    .orElseThrow(() -> internalServerError("No reward found for currency %s".formatted(e.getKey())));

                            return new MoneyLinkResponse()
                                    .amount(total)
                                    .dollarsEquivalent(totalEquivalent)
                                    .currencyCode(currencyCode)
                                    .currencyName(currencyName)
                                    .currencyLogoUrl(currencyLogoUrl);
                        }
                )
                .sorted(comparing(MoneyLinkResponse::getCurrencyCode))
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

    static ProjectPage mapProjectPageToContract(final Page<ProjectView> projectViewPage, int pageIndex) {
        return new ProjectPage()
                .projects(projectViewPage.getContent().stream().map(project -> new ProjectPageItemResponse()
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

    static TransactionalCurrency mapTransactionalCurrency(PayableCurrency currency) {
        return new TransactionalCurrency()
                .id(currency.id().value())
                .code(currency.code().toString())
                .name(currency.name())
                .logoUrl(currency.logoUrl().orElse(null))
                .type(mapCurrencyType(currency.type()))
                .standard(currency.standard().map(BackOfficeMapper::mapCurrencyStandard).orElse(null))
                .blockchain(currency.blockchain().map(BackOfficeMapper::mapBlockchain).orElse(null))
                .address(currency.address().map(ContractAddress::toString).orElse(null));
    }

    static RewardDetailsView.Status mapRewardStatus(RewardStatus rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_INVOICE -> RewardDetailsView.Status.PENDING_INVOICE;
            case PENDING_SIGNUP -> RewardDetailsView.Status.PENDING_SIGNUP;
            case PENDING_CONTRIBUTOR -> RewardDetailsView.Status.PENDING_CONTRIBUTOR;
            case PENDING_VERIFICATION -> RewardDetailsView.Status.PENDING_VERIFICATION;
            case MISSING_PAYOUT_INFO -> RewardDetailsView.Status.MISSING_PAYOUT_INFO;
            case PROCESSING -> RewardDetailsView.Status.PROCESSING;
            case COMPLETE -> RewardDetailsView.Status.COMPLETE;
            case LOCKED -> RewardDetailsView.Status.LOCKED;
        };
    }

    static KycResponse mapShortBillingProfileAdminToKyc(final ShortBillingProfileAdminView view) {
        final Kyc kyc = view.kyc();
        return isNull(kyc) ? null :  new KycResponse()
                .address(kyc.getAddress())
                .birthdate(DateMapper.toZoneDateTime(kyc.getBirthdate()))
                .firstName(kyc.getFirstName())
                .lastName(kyc.getLastName())
                .idDocumentNumber(kyc.getIdDocumentNumber())
                .idDocumentCountryCode(kyc.getIdDocumentCountryCode())
                .idDocumentType(switch (kyc.getIdDocumentType()) {
                    case ID_CARD -> KycResponse.IdDocumentTypeEnum.ID_CARD;
                    case PASSPORT -> KycResponse.IdDocumentTypeEnum.PASSPORT;
                    case DRIVER_LICENSE -> KycResponse.IdDocumentTypeEnum.DRIVER_LICENSE;
                    case RESIDENCE_PERMIT -> KycResponse.IdDocumentTypeEnum.RESIDENCE_PERMIT;
                })
                .country(kyc.getCountry().display().orElseGet(() -> kyc.getCountry().iso3Code()))
                .usCitizen(kyc.getUsCitizen())
                .sumsubUrl("https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(kyc.getExternalApplicantId()))
                .validUntil(DateMapper.toZoneDateTime(kyc.getValidUntil()));
    }

    static KybResponse mapShortBillingProfileAdminToKyb(final ShortBillingProfileAdminView view) {
        final Kyb kyb = view.kyb();
        return isNull(view.kyb()) ? null : new KybResponse()
                .address(kyb.getAddress())
                .countryCode(kyb.getCountry().display().orElseGet(() -> kyb.getCountry().iso3Code()))
                .name(kyb.getName())
                .euVATNumber(kyb.getEuVATNumber())
                .registrationNumber(kyb.getRegistrationNumber())
                .registrationDate(DateMapper.toZoneDateTime(kyb.getRegistrationDate()))
                .usEntity(kyb.getUsEntity())
                .sumsubUrl("https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(kyb.getExternalApplicantId()))
                .subjectToEuropeVAT(kyb.getSubjectToEuropeVAT());
    }
}
