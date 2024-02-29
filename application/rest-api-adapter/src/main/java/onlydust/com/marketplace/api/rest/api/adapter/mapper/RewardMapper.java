package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;
import onlydust.com.marketplace.project.domain.view.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;

import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectBudgetMapper.mapCurrency;

public interface RewardMapper {

    static OldRequestRewardCommand rewardRequestToDomain(final RewardRequest rewardRequest, final UUID projectId) {
        return OldRequestRewardCommand.builder()
                .amount(rewardRequest.getAmount())
                .projectId(projectId)
                .currency(mapCurrency(rewardRequest.getCurrency()))
                .recipientId(rewardRequest.getRecipientId())
                .items(rewardRequest.getItems().stream().map(RewardMapper::rewardItemRequestToDomain).toList())
                .build();
    }

    private static OldRequestRewardCommand.Item rewardItemRequestToDomain(final RewardItemRequest rewardItemRequest) {
        return OldRequestRewardCommand.Item.builder()
                .id(rewardItemRequest.getId())
                .number(rewardItemRequest.getNumber())
                .repoId(rewardItemRequest.getRepoId())
                .type(switch (rewardItemRequest.getType()) {
                    case ISSUE -> OldRequestRewardCommand.Item.Type.issue;
                    case PULL_REQUEST -> OldRequestRewardCommand.Item.Type.pullRequest;
                    case CODE_REVIEW -> OldRequestRewardCommand.Item.Type.codeReview;
                })
                .build();
    }

    static RewardDetailsResponse rewardDetailsToResponse(RewardView rewardView) {
        return new RewardDetailsResponse()
                .from(new ContributorResponse()
                        .githubUserId(rewardView.getFrom().getGithubUserId())
                        .avatarUrl(rewardView.getFrom().getGithubAvatarUrl())
                        .login(rewardView.getFrom().getGithubLogin())
                )
                .to(
                        new ContributorResponse()
                                .githubUserId(rewardView.getTo().getGithubUserId())
                                .avatarUrl(rewardView.getTo().getGithubAvatarUrl())
                                .login(rewardView.getTo().getGithubLogin())
                )
                .createdAt(DateMapper.toZoneDateTime(rewardView.getCreatedAt()))
                .processedAt(DateMapper.toZoneDateTime(rewardView.getProcessedAt()))
                .amount(rewardView.getAmount())
                .currency(mapCurrency(rewardView.getCurrency()))
                .status(mapRewardStatus(rewardView.getStatus()))
                .unlockDate(DateMapper.toZoneDateTime(rewardView.getUnlockDate()))
                .dollarsEquivalent(rewardView.getDollarsEquivalent())
                .id(rewardView.getId())
                .receipt(receiptToResponse(rewardView.getReceipt()))
                .project(ProjectMapper.mapShortProjectResponse(rewardView.getProject()));
    }

    @NonNull
    private static RewardStatus mapRewardStatus(UserRewardStatus rewardView) {
        return switch (rewardView) {
            case complete -> RewardStatus.COMPLETE;
            case missingPayoutInfo -> RewardStatus.MISSING_PAYOUT_INFO;
            case pendingInvoice -> RewardStatus.PENDING_INVOICE;
            case processing -> RewardStatus.PROCESSING;
            case locked -> RewardStatus.LOCKED;
            case pendingVerification -> RewardStatus.PENDING_VERIFICATION;
        };
    }

    @NonNull
    private static RewardStatus mapRewardStatusToProject(UserRewardStatus rewardView) {
        return switch (rewardView) {
            case complete -> RewardStatus.COMPLETE;
            case missingPayoutInfo -> RewardStatus.PENDING_CONTRIBUTOR;
            case locked -> RewardStatus.LOCKED;
            case pendingVerification -> RewardStatus.PENDING_CONTRIBUTOR;
            default -> RewardStatus.PROCESSING;
        };
    }


    static RewardResponse rewardToResponse(ContributionRewardView rewardView) {
        return new RewardResponse()
                .from(new ContributorResponse()
                        .githubUserId(rewardView.getFrom().getGithubUserId())
                        .avatarUrl(rewardView.getFrom().getGithubAvatarUrl())
                        .login(rewardView.getFrom().getGithubLogin())
                )
                .to(
                        new ContributorResponse()
                                .githubUserId(rewardView.getTo().getGithubUserId())
                                .avatarUrl(rewardView.getTo().getGithubAvatarUrl())
                                .login(rewardView.getTo().getGithubLogin())
                )
                .createdAt(DateMapper.toZoneDateTime(rewardView.getCreatedAt()))
                .processedAt(DateMapper.toZoneDateTime(rewardView.getProcessedAt()))
                .amount(rewardView.getAmount())
                .currency(mapCurrency(rewardView.getCurrency()))
                .status(mapRewardStatusToProject(rewardView.getStatus()))
                .dollarsEquivalent(rewardView.getDollarsEquivalent())
                .id(rewardView.getId())
                ;
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
}
