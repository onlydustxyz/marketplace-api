package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.notification.UserRegisteredOnHackathon;
import onlydust.com.marketplace.project.domain.port.input.HackathonObserverPort;

import java.util.UUID;

@AllArgsConstructor
public class HackathonObserver implements HackathonObserverPort {
    private final NotificationPort notificationPort;

    @Override
    public void onUserRegistration(Hackathon.Id hackathonId, UUID userId) {
        notificationPort.notify(new UserRegisteredOnHackathon(userId, hackathonId));
    }
}
