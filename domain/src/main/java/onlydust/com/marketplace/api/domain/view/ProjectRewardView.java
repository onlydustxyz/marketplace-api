package onlydust.com.marketplace.api.domain.view;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

@Data
@Builder
public class ProjectRewardView {

  UUID id;
  Integer numberOfRewardedContributions;
  Date requestedAt;
  String rewardedUserLogin;
  String rewardedUserAvatar;
  RewardStatusView status;
  RewardAmountView amount;


  @Data
  @Builder
  public static class RewardAmountView {

    BigDecimal total;
    Currency currency;
    BigDecimal dollarsEquivalent;
  }

  public enum RewardStatusView {
    pendingSignup, processing, complete
  }

  public enum SortBy {
    requestedAt, status, contribution, amount
  }

  @Builder
  @Data
  public static class Filters {

    @Builder.Default
    List<Currency> currencies = List.of();
    @Builder.Default
    List<Long> contributors = List.of();
    Date from;
    Date to;
  }
}
