package onlydust.com.marketplace.api.postgres.adapter.entity.json;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.util.List;
import java.util.UUID;

public record HackathonTrack(
        @NonNull String name,
        String subtitle,
        String description,
        String iconSlug,
        @NonNull List<UUID> projectIds
) {
    public static HackathonTrack of(Hackathon.Track track) {
        return new HackathonTrack(track.name(), track.subtitle(), track.description(), track.iconSlug(), track.projectIds());
    }

    public Hackathon.Track toDomain() {
        return new Hackathon.Track(this.name, this.subtitle, this.description, this.iconSlug, this.projectIds());
    }
}
