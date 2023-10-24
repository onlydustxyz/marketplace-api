package onlydust.com.marketplace.api.od.rust.api.client.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RequestRewardDTO {
    UUID projectId;
    Long recipientId;
    BigDecimal amount;
    String currency;
    ReasonDTO reason;

    @Data
    @Builder
    public static class ReasonDTO {
        List<WorkItemDTO> workItems;

        @Data
        @Builder
        public static class WorkItemDTO {
            String id;
            String type;
            Long repoId;
            Long number;
        }
    }
}
