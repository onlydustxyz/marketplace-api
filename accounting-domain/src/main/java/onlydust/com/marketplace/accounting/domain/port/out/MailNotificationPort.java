package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;

import java.util.List;

public interface MailNotificationPort {
    void sendRewardsPaidMail(@NonNull String email, @NonNull List<BackofficeRewardView> rewardViews);
}
