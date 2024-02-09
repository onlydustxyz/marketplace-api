package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record Reward(
        @NonNull UUID id,
        @NonNull UUID projectId,
        @NonNull UUID requestorId,
        @NonNull Long recipientId,
        @NonNull BigDecimal amount,
        @NonNull Currency currency,
        @NonNull Date requestedAt,
        Date invoiceReceivedAt,
        @NonNull List<Item> rewardItems) {

    @Builder
    @Data
    @Accessors(fluent = true)
    public static class Item {
        String id;
        Long number;
        Long repoId;
        Type type;

        public enum Type {
            PULL_REQUEST, ISSUE, CODE_REVIEW
        }
    }
}
