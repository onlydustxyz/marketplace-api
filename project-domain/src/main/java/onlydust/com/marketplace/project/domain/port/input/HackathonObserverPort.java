package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Hackathon;

public interface HackathonObserverPort {

    void onUserRegistration(final Hackathon.Id hackathonId, final UserId userId);
}
