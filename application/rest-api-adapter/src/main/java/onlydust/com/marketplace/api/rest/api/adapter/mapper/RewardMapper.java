package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.view.ContributionRewardView;
import onlydust.com.marketplace.project.domain.view.ReceiptView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper.toMoney;

public interface RewardMapper {

    static RequestRewardCommand rewardRequestToDomain(final RewardRequest rewardRequest, final ProjectId projectId) {
        return RequestRewardCommand.builder()
                .amount(rewardRequest.getAmount())
                .projectId(projectId)
                .currencyId(CurrencyView.Id.of(rewardRequest.getCurrencyId()))
                .recipientId(rewardRequest.getRecipientId())
                .items(rewardRequest.getItems().stream().map(RewardMapper::rewardItemRequestToDomain).toList())
                .build();
    }

    private static RequestRewardCommand.Item rewardItemRequestToDomain(final RewardItemRequest rewardItemRequest) {
        return RequestRewardCommand.Item.builder()
                .id(rewardItemRequest.getId())
                .number(rewardItemRequest.getNumber())
                .repoId(rewardItemRequest.getRepoId())
                .type(switch (rewardItemRequest.getType()) {
                    case ISSUE -> RequestRewardCommand.Item.Type.issue;
                    case PULL_REQUEST -> RequestRewardCommand.Item.Type.pullRequest;
                    case CODE_REVIEW -> RequestRewardCommand.Item.Type.codeReview;
                })
                .build();
    }

    static RewardDetailsResponse commonRewardDetailsToResponse(RewardDetailsView rewardDetailsView) {
        return new RewardDetailsResponse()
                .from(new ContributorResponse()
                        .githubUserId(rewardDetailsView.getFrom().getGithubUserId())
                        .avatarUrl(rewardDetailsView.getFrom().getAvatarUrl())
                        .login(rewardDetailsView.getFrom().getLogin())
                        .isRegistered(rewardDetailsView.getFrom().getIsRegistered())
                )
                .to(new ContributorResponse()
                        .githubUserId(rewardDetailsView.getTo().getGithubUserId())
                        .avatarUrl(rewardDetailsView.getTo().getAvatarUrl())
                        .login(rewardDetailsView.getTo().getLogin())
                        .isRegistered(rewardDetailsView.getTo().getIsRegistered())
                )
                .createdAt(DateMapper.toZoneDateTime(rewardDetailsView.getCreatedAt()))
                .processedAt(DateMapper.toZoneDateTime(rewardDetailsView.getProcessedAt()))
                .amount(toMoney(rewardDetailsView.getAmount()))
                .unlockDate(DateMapper.toZoneDateTime(rewardDetailsView.getUnlockDate()))
                .id(rewardDetailsView.getId())
                .receipt(receiptToResponse(rewardDetailsView.getReceipt()))
                .project(ProjectMapper.mapShortProjectResponse(rewardDetailsView.getProject()))
                .billingProfileId(rewardDetailsView.getBillingProfileId());
    }

    static RewardDetailsResponse projectRewardDetailsToResponse(RewardDetailsView rewardDetailsView, AuthenticatedUser authenticatedUser) {
        return commonRewardDetailsToResponse(rewardDetailsView)
                .status(map(rewardDetailsView.getStatus().as(authenticatedUser)));
    }

    static RewardDetailsResponse myRewardDetailsToResponse(RewardDetailsView rewardDetailsView, AuthenticatedUser authenticatedUser) {
        return commonRewardDetailsToResponse(rewardDetailsView)
                .status(map(rewardDetailsView.getStatus().as(authenticatedUser)));
    }

    static RewardStatusContract map(RewardStatus.Output status) {
        return switch (status) {
            case PENDING_SIGNUP -> RewardStatusContract.PENDING_SIGNUP;
            case PENDING_CONTRIBUTOR -> RewardStatusContract.PENDING_CONTRIBUTOR;
            case PENDING_BILLING_PROFILE -> RewardStatusContract.PENDING_BILLING_PROFILE;
            case PENDING_COMPANY -> RewardStatusContract.PENDING_COMPANY;
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

    static RewardResponse rewardToResponse(ContributionRewardView rewardView, AuthenticatedUser authenticatedUser) {
        return new RewardResponse()
                .from(new ContributorResponse()
                        .githubUserId(rewardView.getFrom().githubUserId())
                        .avatarUrl(rewardView.getFrom().avatarUrl())
                        .login(rewardView.getFrom().login())
                )
                .to(
                        new ContributorResponse()
                                .githubUserId(rewardView.getTo().githubUserId())
                                .avatarUrl(rewardView.getTo().avatarUrl())
                                .login(rewardView.getTo().login())
                )
                .createdAt(DateMapper.toZoneDateTime(rewardView.getCreatedAt()))
                .processedAt(DateMapper.toZoneDateTime(rewardView.getProcessedAt()))
                .amount(toMoney(rewardView.getAmount()))
                .id(rewardView.getId())
                .status(map(rewardView.getStatus().as(authenticatedUser)));
    }

    static ReceiptResponse receiptToResponse(final ReceiptView receiptView) {
        return isNull(receiptView) ? null : new ReceiptResponse()
                .ens(receiptView.getEns())
                .type(receiptView.getType() == ReceiptView.Type.FIAT ? ReceiptType.FIAT : ReceiptType.CRYPTO)
                .iban(receiptView.getIban())
                .walletAddress(receiptView.getWalletAddress())
                .transactionReference(receiptView.getTransactionReference())
                .transactionReferenceLink(receiptView.getTransactionReferenceUrl().orElse(null));
    }

    static RewardItemsPageResponse pageToResponse(final int pageIndex, Page<RewardItemView> page) {
        return new RewardItemsPageResponse()
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()))
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()))
                .rewardItems(page.getContent().stream().map(RewardMapper::itemToResponse).toList())
                ;
    }

    private static RewardItemResponse itemToResponse(final RewardItemView view) {
        return new RewardItemResponse()
                .id(view.getId())
                .contributionId(view.getContributionId())
                .type(switch (view.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                })
                .status(switch (view.getStatus()) {
                    case CANCELLED -> GithubStatus.CANCELLED;
                    case PENDING -> GithubStatus.PENDING;
                    case DRAFT -> GithubStatus.DRAFT;
                    case OPEN -> GithubStatus.OPEN;
                    case MERGED -> GithubStatus.MERGED;
                    case CLOSED -> GithubStatus.CLOSED;
                    case COMMENTED -> GithubStatus.COMMENTED;
                    case APPROVED -> GithubStatus.APPROVED;
                    case CHANGES_REQUESTED -> GithubStatus.CHANGES_REQUESTED;
                    case COMPLETED -> GithubStatus.COMPLETED;
                    case DISMISSED -> GithubStatus.DISMISSED;
                })
                .createdAt(DateMapper.toZoneDateTime(view.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(view.getCompletedAt()))
                .commentsCount(view.getCommentsCount())
                .commitsCount(view.getCommitsCount())
                .userCommitsCount(view.getUserCommitsCount())
                .number(view.getNumber())
                .repoName(view.getRepoName())
                .githubUrl(view.getGithubUrl())
                .title(view.getTitle())
                .githubAuthorId(view.getGithubAuthorId())
                .authorAvatarUrl(view.getAuthorAvatarUrl())
                .authorGithubUrl(view.getAuthorGithubUrl())
                .authorLogin(view.getAuthorLogin())
                .githubBody(view.getGithubBody());
    }

    static ShortCurrencyResponse mapCurrency(CurrencyView currency) {
        if (currency == null) return null;

        return new ShortCurrencyResponse()
                .id(currency.id().value())
                .code(currency.code())
                .name(currency.name())
                .decimals(currency.decimals())
                .logoUrl(currency.logoUrl());
    }

    static ShortCurrencyResponse mapCurrency(Currency currency) {
        if (currency == null) return null;

        return new ShortCurrencyResponse()
                .id(currency.id().value())
                .code(currency.code().toString())
                .name(currency.name())
                .decimals(currency.decimals())
                .logoUrl(currency.logoUri().orElse(null));
    }

}
