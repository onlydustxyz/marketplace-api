package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;

public interface MailNotificationPort {
    void sendRewardsPaidMail(@NonNull String email, @NonNull List<RewardView> rewardViews);
}
