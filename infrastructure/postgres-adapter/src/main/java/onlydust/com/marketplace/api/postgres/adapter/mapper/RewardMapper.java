package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.view.CodeReviewOutcome;
import onlydust.com.marketplace.api.domain.view.ReceiptView;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.RewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CryptoReceiptJsonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CryptoReceiptJsonEntity.Crypto;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.FiatReceiptJsonEntity;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper.OBJECT_MAPPER;

public interface RewardMapper {
    static RewardView rewardToDomain(RewardViewEntity rewardViewEntityByd) {
        return RewardView.builder()
                .id(rewardViewEntityByd.getId())
                .to(GithubUserIdentity.builder()
                        .githubAvatarUrl(rewardViewEntityByd.getRecipientAvatarUrl())
                        .githubLogin(rewardViewEntityByd.getRecipientLogin())
                        .githubUserId(rewardViewEntityByd.getRecipientId())
                        .build())
                .amount(rewardViewEntityByd.getAmount())
                .createdAt(rewardViewEntityByd.getRequestedAt())
                .processedAt(rewardViewEntityByd.getProcessedAt())
                .currency(switch (rewardViewEntityByd.getCurrency()) {
                    case op -> Currency.Op;
                    case apt -> Currency.Apt;
                    case eth -> Currency.Eth;
                    case usd -> Currency.Usd;
                    case stark -> Currency.Stark;
                })
                .dollarsEquivalent(rewardViewEntityByd.getDollarsEquivalent())
                .status(switch (rewardViewEntityByd.getStatus()) {
                    case "PENDING_SIGNUP" -> RewardView.Status.pendingSignup;
                    case "COMPLETE" -> RewardView.Status.complete;
                    default -> RewardView.Status.processing;
                })
                .from(GithubUserIdentity.builder()
                        .githubUserId(rewardViewEntityByd.getRequestorId())
                        .githubLogin(rewardViewEntityByd.getRequestorLogin())
                        .githubAvatarUrl(rewardViewEntityByd.getRequestorAvatarUrl())
                        .build())
                .build();
    }

    static RewardView rewardWithReceiptToDomain(RewardViewEntity rewardViewEntityByd) {
        return RewardView.builder()
                .id(rewardViewEntityByd.getId())
                .to(GithubUserIdentity.builder()
                        .githubAvatarUrl(rewardViewEntityByd.getRecipientAvatarUrl())
                        .githubLogin(rewardViewEntityByd.getRecipientLogin())
                        .githubUserId(rewardViewEntityByd.getRecipientId())
                        .build())
                .amount(rewardViewEntityByd.getAmount())
                .createdAt(rewardViewEntityByd.getRequestedAt())
                .processedAt(rewardViewEntityByd.getProcessedAt())
                .currency(switch (rewardViewEntityByd.getCurrency()) {
                    case op -> Currency.Op;
                    case apt -> Currency.Apt;
                    case eth -> Currency.Eth;
                    case usd -> Currency.Usd;
                    case stark -> Currency.Stark;
                })
                .dollarsEquivalent(rewardViewEntityByd.getDollarsEquivalent())
                .status(switch (rewardViewEntityByd.getStatus()) {
                    case "PENDING_SIGNUP" -> RewardView.Status.pendingSignup;
                    case "COMPLETE" -> RewardView.Status.complete;
                    default -> RewardView.Status.processing;
                })
                .from(GithubUserIdentity.builder()
                        .githubUserId(rewardViewEntityByd.getRequestorId())
                        .githubLogin(rewardViewEntityByd.getRequestorLogin())
                        .githubAvatarUrl(rewardViewEntityByd.getRequestorAvatarUrl())
                        .build())
                .receipt(receiptToDomain(rewardViewEntityByd.getReceipt()))
                .build();
    }

    private static ReceiptView receiptToDomain(final JsonNode receipt) {
        if (isNull(receipt)) {
            return null;
        }

        try {
            if (receipt.has("Sepa")) {
                final FiatReceiptJsonEntity fiatReceiptJsonEntity =
                        OBJECT_MAPPER.treeToValue(receipt.get("Sepa"),
                                FiatReceiptJsonEntity.class);
                return ReceiptView.builder()
                        .iban(fiatReceiptJsonEntity.getRecipientIban())
                        .transactionReference(fiatReceiptJsonEntity.getTransactionReference())
                        .type(ReceiptView.Type.FIAT)
                        .build();
            } else if (receipt.has(Crypto.Ethereum.name())) {
                return cryptoReceiptEntityToDomain(Crypto.Ethereum, receipt);
            } else if (receipt.has(Crypto.Aptos.name())) {
                return cryptoReceiptEntityToDomain(Crypto.Aptos, receipt);
            } else if (receipt.has(Crypto.Optimism.name())) {
                return cryptoReceiptEntityToDomain(Crypto.Optimism, receipt);
            } else if (receipt.has(Crypto.Starknet.name())) {
                return cryptoReceiptEntityToDomain(Crypto.Starknet, receipt);
            }
        } catch (JsonProcessingException e) {
            throw OnlyDustException.internalServerError("Failed to deserialized payment receipt", e);
        }

        throw OnlyDustException.internalServerError("Invalid payment receipt format");
    }

    private static ReceiptView cryptoReceiptEntityToDomain(final Crypto crypto,
                                                           final JsonNode receipt) throws JsonProcessingException {
        final CryptoReceiptJsonEntity cryptoReceiptJsonEntity = OBJECT_MAPPER.treeToValue(receipt.get(
                crypto.name()), CryptoReceiptJsonEntity.class);
        return ReceiptView.builder()
                .transactionReference(cryptoReceiptJsonEntity.getTransactionHash())
                .ens(cryptoReceiptJsonEntity.getRecipientEns())
                .walletAddress(cryptoReceiptJsonEntity.getRecipientAddress())
                .type(ReceiptView.Type.CRYPTO)
                .build();
    }


    static RewardItemView itemToDomain(RewardItemViewEntity rewardItemViewEntity) {
        return RewardItemView.builder()
                .id(rewardItemViewEntity.getId())
                .recipientId(rewardItemViewEntity.getRecipientId())
                .authorAvatarUrl(rewardItemViewEntity.getAuthorAvatarUrl())
                .createdAt(rewardItemViewEntity.getCreatedAt())
                .title(rewardItemViewEntity.getTitle())
                .authorGithubUrl(rewardItemViewEntity.getAuthorAvatarUrl())
                .githubAuthorId(rewardItemViewEntity.getAuthorId())
                .githubUrl(rewardItemViewEntity.getGithubUrl())
                .commentsCount(rewardItemViewEntity.getCommentsCount())
                .commitsCount(rewardItemViewEntity.getCommitsCount())
                .userCommitsCount(rewardItemViewEntity.getUserCommitsCount())
                .number(rewardItemViewEntity.getNumber())
                .lastUpdateAt(rewardItemViewEntity.getCompletedAt())
                .type(switch (rewardItemViewEntity.getType()) {
                    case issue -> ContributionType.ISSUE;
                    case pull_request -> ContributionType.PULL_REQUEST;
                    case code_review -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardItemViewEntity.getRepoName())
                .authorLogin(rewardItemViewEntity.getAuthorLogin())
                .status(switch (rewardItemViewEntity.getStatus()) {
                    case canceled -> ContributionStatus.CANCELLED;
                    case complete -> ContributionStatus.COMPLETED;
                    case in_progress -> ContributionStatus.IN_PROGRESS;
                })
                .outcome(isNull(rewardItemViewEntity.getOutcome()) ? null : switch (rewardItemViewEntity.getOutcome()) {
                    case approved -> CodeReviewOutcome.approved;
                    case change_requested -> CodeReviewOutcome.changeRequested;
                })
                .build();
    }
}
