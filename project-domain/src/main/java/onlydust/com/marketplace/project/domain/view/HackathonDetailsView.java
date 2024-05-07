package onlydust.com.marketplace.project.domain.view;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.time.ZonedDateTime;
import java.util.List;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode
public class HackathonDetailsView {

    @NonNull
    Hackathon.Id id;
    @NonNull
    String slug;
    @NonNull
    Hackathon.Status status;
    @NonNull
    String title;
    @NonNull
    String subtitle;
    String description;
    String location;
    String totalBudget;
    @NonNull
    ZonedDateTime startDate;
    @NonNull
    ZonedDateTime endDate;
    @NonNull
    List<NamedLink> links;
    @NonNull
    List<Sponsor> sponsors;
    @NonNull
    List<Track> tracks;
    @NonNull
    List<ProjectShortView> projects;
    @NonNull
    List<RegisteredContributorLinkView> registeredUsers;

    public record Track(
            @NonNull String name,
            String subtitle,
            String description,
            String iconSlug,
            @NonNull List<ProjectShortView> projects
    ) {
    }
}
