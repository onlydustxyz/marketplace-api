package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import onlydust.com.marketplace.project.domain.port.input.HackathonObserverPort;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;

import java.time.ZonedDateTime;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class HackathonService implements HackathonFacadePort {
    private final HackathonStoragePort hackathonStoragePort;
    private final HackathonObserverPort hackathonObserverPort;

    @Override
    public void createHackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate,
                                @NonNull ZonedDateTime endDate) {
        final var hackathon = new Hackathon(title, subtitle, startDate, endDate);
        hackathonStoragePort.save(hackathon);
    }

    @Override
    public void updateHackathon(@NonNull Hackathon hackathon) {
        if (!hackathonStoragePort.exists(hackathon.id()))
            throw notFound("Hackathon %s not found".formatted(hackathon.id()));
        hackathonStoragePort.save(hackathon);
    }

    @Override
    public void updateHackathonStatus(@NonNull UUID hackathonId, Hackathon.@NonNull Status status) {
        if (!hackathonStoragePort.exists(Hackathon.Id.of(hackathonId)))
            throw notFound("Hackathon %s not found".formatted(hackathonId));
        hackathonStoragePort.saveStatus(Hackathon.Id.of(hackathonId), status);
    }

    @Override
    public void deleteHackathon(Hackathon.Id hackathonId) {
        hackathonStoragePort.delete(hackathonId);
    }

    @Override
    public void registerToHackathon(UUID userId, Hackathon.Id hackathonId) {
        if (!hackathonStoragePort.exists(hackathonId))
            throw notFound("Hackathon %s not found".formatted(hackathonId));
        hackathonStoragePort.registerUser(userId, hackathonId);
        hackathonObserverPort.onUserRegistration(hackathonId, userId);
    }

}
