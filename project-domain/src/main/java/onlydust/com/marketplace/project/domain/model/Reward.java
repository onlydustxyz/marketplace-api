package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Value
@Accessors(fluent = true)
public class Reward {
    @NonNull UUID id;
    @NonNull UUID projectId;
    @NonNull UUID requestorId;
    @NonNull Long recipientId;
    @NonNull BigDecimal amount;
    @NonNull CurrencyView.Id currencyId;
    @NonNull Date requestedAt;
    @NonNull List<Item> rewardItems;

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

    public enum SortBy {
        REQUESTED_AT, STATUS, CONTRIBUTION, AMOUNT
    }
}
