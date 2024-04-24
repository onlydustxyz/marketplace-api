package onlydust.com.marketplace.accounting.domain.events.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import java.math.BigDecimal;
@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ShortReward {
    RewardId id;
    String projectName;
    String currencyCode;
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
}
