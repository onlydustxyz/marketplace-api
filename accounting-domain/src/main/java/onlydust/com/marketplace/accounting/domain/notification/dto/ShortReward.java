package onlydust.com.marketplace.accounting.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.math.BigDecimal;

@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public record ShortReward(RewardId id,
                          String projectName,
                          String currencyCode,
                          BigDecimal amount,
                          BigDecimal dollarsEquivalent,
                          Integer contributionsCount,
                          String sentByGithubLogin
) {

}
