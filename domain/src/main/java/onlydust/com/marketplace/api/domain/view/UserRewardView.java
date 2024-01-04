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
public class UserRewardView {

  UUID id;
  UUID projectId;
  Integer numberOfRewardedContributions;
  Date requestedAt;
  String rewardedOnProjectName;
  String rewardedOnProjectLogoUrl;
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
    pendingInvoice, processing, complete, missingPayoutInfo
  }

  public enum SortBy {
    requestedAt, status, contribution, amount
  }

  @Data
  @Builder
  public static class Filters {

    List<Currency> currencies;
    List<UUID> projectIds;
    Date from;
    Date to;
  }
}
