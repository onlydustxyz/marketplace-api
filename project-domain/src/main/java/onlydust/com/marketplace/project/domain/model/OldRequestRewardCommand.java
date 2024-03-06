package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class OldRequestRewardCommand {
    Long recipientId;
    BigDecimal amount;
    Currency currency;
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
