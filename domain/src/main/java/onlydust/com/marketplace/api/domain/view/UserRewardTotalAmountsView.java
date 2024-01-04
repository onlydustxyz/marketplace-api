package onlydust.com.marketplace.api.domain.view;

import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRewardTotalAmountsView {

  @Builder.Default
  List<UserTotalRewardView> userTotalRewards = new ArrayList<>();


  public BigDecimal getTotalAmount() {
    BigDecimal totalAmount = null;
    for (UserTotalRewardView userTotalReward : this.userTotalRewards) {
      if (nonNull(userTotalReward.getTotalDollarsEquivalent())) {
        totalAmount = Optional.ofNullable(totalAmount).orElse(BigDecimal.ZERO)
            .add(userTotalReward.getTotalDollarsEquivalent());
      }
    }
    return totalAmount;
  }

  public void addUserTotalReward(final UserTotalRewardView userTotalRewardView) {
    this.userTotalRewards.add(userTotalRewardView);
  }
}
