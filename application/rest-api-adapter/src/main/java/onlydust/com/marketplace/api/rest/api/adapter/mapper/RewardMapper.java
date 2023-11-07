package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.view.ReceiptView;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.RewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface RewardMapper {

    static RequestRewardCommand rewardRequestToDomain(final RewardRequest rewardRequest, final UUID projectId) {
        return RequestRewardCommand.builder()
                .amount(rewardRequest.getAmount())
                .projectId(projectId)
                .currency(switch (rewardRequest.getCurrency()) {
                    case OP -> Currency.Op;
                    case APT -> Currency.Apt;
                    case USD -> Currency.Usd;
                    case ETH -> Currency.Eth;
                    case STARK -> Currency.Stark;
                })
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

    static RewardDetailsResponse rewardDetailsToResponse(RewardView rewardView) {
        return new RewardDetailsResponse()
                .from(new GithubUserResponse()
                        .id(rewardView.getFrom().getGithubUserId())
                        .avatarUrl(rewardView.getFrom().getGithubAvatarUrl())
                        .login(rewardView.getFrom().getGithubLogin())
                )
                .to(
                        new GithubUserResponse()
                                .id(rewardView.getTo().getGithubUserId())
                                .avatarUrl(rewardView.getTo().getGithubAvatarUrl())
                                .login(rewardView.getTo().getGithubLogin())
                )
                .createdAt(DateMapper.toZoneDateTime(rewardView.getCreatedAt()))
                .processedAt(DateMapper.toZoneDateTime(rewardView.getProcessedAt()))
                .amount(rewardView.getAmount())
                .currency(switch (rewardView.getCurrency()) {
                    case Stark -> CurrencyContract.STARK;
                    case Apt -> CurrencyContract.APT;
                    case Op -> CurrencyContract.OP;
                    case Eth -> CurrencyContract.ETH;
                    case Usd -> CurrencyContract.USD;
                })
                .status(switch (rewardView.getStatus()) {
                    case complete -> RewardStatus.COMPLETE;
                    default -> RewardStatus.PROCESSING;
                    case pendingSignup -> RewardStatus.PENDING_SIGNUP;
                })
                .dollarsEquivalent(rewardView.getDollarsEquivalent())
                .id(rewardView.getId())
                .receipt(receiptToResponse(rewardView.getReceipt()))
                ;
    }

    static ReceiptResponse receiptToResponse(final ReceiptView receiptView) {
        return isNull(receiptView) ? null : new ReceiptResponse()
                .ens(receiptView.getEns())
                .type(switch (receiptView.getType()) {
                    case FIAT -> ReceiptType.FIAT;
                    case CRYPTO -> ReceiptType.CRYPTO;
                })
                .iban(receiptView.getIban())
                .walletAddress(receiptView.getWalletAddress())
                .transactionReference(receiptView.getTransactionReference());
    }

    static RewardItemsPageResponse pageToResponse(final int pageIndex, Page<RewardItemView> page) {
        final RewardItemsPageResponse rewardItemsPageResponse = new RewardItemsPageResponse();
        rewardItemsPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        rewardItemsPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        rewardItemsPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        rewardItemsPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        page.getContent().stream()
                .map(RewardMapper::itemToResponse)
                .forEach(rewardItemsPageResponse::addRewardItemsItem);
        return rewardItemsPageResponse;
    }

    private static RewardItemResponse itemToResponse(final RewardItemView view) {
        return new RewardItemResponse()
                .id(view.getId())
                .type(switch (view.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                })
                .status(switch (view.getStatus()) {
                    case CANCELLED -> RewardItemStatus.CANCELLED;
                    case PENDING -> RewardItemStatus.PENDING;
                    case DRAFT -> RewardItemStatus.DRAFT;
                    case OPEN -> RewardItemStatus.OPEN;
                    case MERGED -> RewardItemStatus.MERGED;
                    case CLOSED -> RewardItemStatus.CLOSED;
                    case CHANGES_REQUESTED -> RewardItemStatus.CHANGES_REQUESTED;
                    case COMPLETED -> RewardItemStatus.COMPLETED;
                })
                .createdAt(DateMapper.toZoneDateTime(view.getCreatedAt()))
                .lastUpdateAt(DateMapper.toZoneDateTime(view.getLastUpdateAt()))
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
                .codeReviewOutcome(isNull(view.getOutcome()) ? null : switch (view.getOutcome()) {
                    case approved -> RewardItemResponse.CodeReviewOutcomeEnum.APPROVED;
                    case changeRequested -> RewardItemResponse.CodeReviewOutcomeEnum.CHANGE_REQUESTED;
                })
                .authorLogin(view.getAuthorLogin());
    }
}
