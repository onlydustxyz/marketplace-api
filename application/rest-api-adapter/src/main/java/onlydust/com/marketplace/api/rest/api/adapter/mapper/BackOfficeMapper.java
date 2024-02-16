package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.api.domain.model.Ecosystem;
import onlydust.com.marketplace.api.domain.view.backoffice.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.time.ZoneOffset;

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

    static CurrencyCode mapCurrency(final onlydust.com.marketplace.api.domain.model.Currency currency) {
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
                        .verificationStatus(switch (user.getVerificationStatus()) {
                            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
                            case STARTED -> VerificationStatus.STARTED;
                            case REJECTED -> VerificationStatus.REJECTED;
                            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
                            case CLOSED -> VerificationStatus.CLOSED;
                            case VERIFIED -> VerificationStatus.VERIFIED;
                            case INVALIDATED -> VerificationStatus.INVALIDATED;
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

    static ProjectVisibility mapProjectVisibility(onlydust.com.marketplace.api.domain.model.ProjectVisibility visibility) {
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
}
