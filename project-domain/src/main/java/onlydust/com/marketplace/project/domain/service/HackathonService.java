package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class HackathonService implements HackathonFacadePort {
    private final HackathonStoragePort hackathonStoragePort;

    @Override
    public HackathonDetailsView createHackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate,
                                                @NonNull ZonedDateTime endDate) {
        final var hackathon = new Hackathon(title, subtitle, startDate, endDate);
        hackathonStoragePort.save(hackathon);
        return getHackathonById(hackathon.id());
    }

    @Override
    public HackathonDetailsView getHackathonById(@NonNull Hackathon.Id hackathonId) {
        return hackathonStoragePort.findById(hackathonId)
                .orElseThrow(() -> OnlyDustException.notFound("Hackathon %s not found".formatted(hackathonId)));
    }
}
