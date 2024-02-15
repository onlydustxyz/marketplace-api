package onlydust.com.marketplace.api.bootstrap.helper;

import lombok.Getter;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.port.output.NotificationPort;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SlackNotificationStub implements NotificationPort {

    private final List<BillingProfileUpdated> notifications = new ArrayList<>();

    @Override
    public void notifyNewVerificationEvent(BillingProfileUpdated billingProfileUpdated) {
       this.notifications.add(billingProfileUpdated);
    }


}
