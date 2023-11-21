package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.view.*;
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
                    case ISSUE -> ContributionType.ISSUE;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardItemViewEntity.getRepoName())
                .authorLogin(rewardItemViewEntity.getAuthorLogin())
                .status(githubStatusToDomain(rewardItemViewEntity.getStatus()))
                .build();
    }

    static RewardItemStatus githubStatusToDomain(final String status) {
        return switch (status) {
            case "COMPLETED" -> RewardItemStatus.COMPLETED;
            case "CANCELLED" -> RewardItemStatus.CANCELLED;
            case "CLOSED" -> RewardItemStatus.CLOSED;
            case "MERGED" -> RewardItemStatus.MERGED;
            case "DRAFT" -> RewardItemStatus.DRAFT;
            case "PENDING" -> RewardItemStatus.PENDING;
            case "COMMENTED" -> RewardItemStatus.COMMENTED;
            case "APPROVED" -> RewardItemStatus.APPROVED;
            case "CHANGES_REQUESTED" -> RewardItemStatus.CHANGES_REQUESTED;
            case "DISMISSED" -> RewardItemStatus.DISMISSED;
            default -> RewardItemStatus.OPEN;
        };
    }

    static UserRewardView.RewardStatusView mapStatusForUser(String status) {
        return switch (status) {
            case "PENDING_INVOICE" -> UserRewardView.RewardStatusView.pendingInvoice;
            case "COMPLETE" -> UserRewardView.RewardStatusView.complete;
            case "MISSING_PAYOUT_INFO" -> UserRewardView.RewardStatusView.missingPayoutInfo;
            default -> UserRewardView.RewardStatusView.processing;
        };
    }

    static ProjectRewardView.RewardStatusView mapStatusForProject(String status) {
        return switch (status) {
            case "PENDING_SIGNUP" -> ProjectRewardView.RewardStatusView.pendingSignup;
            case "COMPLETE" -> ProjectRewardView.RewardStatusView.complete;
            default -> ProjectRewardView.RewardStatusView.processing;
        };
    }
}
