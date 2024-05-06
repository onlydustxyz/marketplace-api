package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.util.UUID;

public interface HackathonObserverPort {

    void onUserRegistration(final Hackathon.Id hackathonId, final UUID userId);
}
