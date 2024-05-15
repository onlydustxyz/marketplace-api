package onlydust.com.marketplace.api.bootstrap.helper;

import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.notification.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.model.notification.UserRegisteredOnHackathon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class SlackNotificationStub implements NotificationPort {

    private final List<BillingProfileVerificationUpdated> billingProfileNotifications = Collections.synchronizedList(new ArrayList<>());
    private final List<UserRegisteredOnHackathon> hackathonNotifications = Collections.synchronizedList(new ArrayList<>());
    private final List<ProjectCategorySuggestion> projectCategorySuggestionNotifications = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void notify(Event event) {
        if (event instanceof BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
            this.billingProfileNotifications.add(billingProfileVerificationUpdated);
        }
        if (event instanceof UserRegisteredOnHackathon userRegisteredOnHackathon) {
            this.hackathonNotifications.add(userRegisteredOnHackathon);
        }
        if (event instanceof ProjectCategorySuggestion projectCategorySuggestion) {
            this.projectCategorySuggestionNotifications.add(projectCategorySuggestion);
        }
    }

    public void reset() {
        this.billingProfileNotifications.clear();
        this.hackathonNotifications.clear();
    }
}
