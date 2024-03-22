package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class RequestRewardCommand {
    Long recipientId;
    BigDecimal amount;
    CurrencyView.Id currencyId;
    List<Item> items;
    UUID projectId;

    @Builder
    @Data
    public static class Item {
        String id;
        Long number;
        Long repoId;
        Type type;

        public enum Type {
            pullRequest, issue, codeReview
        }
    }
}
