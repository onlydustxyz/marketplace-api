package onlydust.com.marketplace.project.domain.service;

import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.notification.UserRegisteredOnHackathon;
import onlydust.com.marketplace.project.domain.observer.HackathonObserver;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HackathonObserverTest {

    @Test
    void should_push_event() {
        // Given
        final NotificationPort notificationPort = mock(NotificationPort.class);
        final HackathonObserver hackathonObserver = new HackathonObserver(notificationPort);
        final UUID userId = UUID.randomUUID();
        final Hackathon.Id hackathonId = Hackathon.Id.random();

        // When
        hackathonObserver.onUserRegistration(hackathonId, userId);

        // Then
        verify(notificationPort).notify(
                new UserRegisteredOnHackathon(userId, hackathonId)
        );
    }
}
