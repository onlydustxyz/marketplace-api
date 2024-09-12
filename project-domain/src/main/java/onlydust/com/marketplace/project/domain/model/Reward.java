package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Value
@Accessors(fluent = true)
public class Reward {
    @NonNull
    RewardId id;
    @NonNull
    ProjectId projectId;
    @NonNull
    UserId requestorId;
    @NonNull
    Long recipientId;
    @NonNull
    BigDecimal amount;
    @NonNull
    CurrencyView.Id currencyId;
    @NonNull
    Date requestedAt;
    @NonNull
    List<Item> rewardItems;

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
