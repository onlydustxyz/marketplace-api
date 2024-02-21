package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.project.domain.view.ProjectSponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public interface SponsorMapper {

    static ProjectSponsorView mapToSponsor(final ProjectSponsorEntity entity) {
        final var sponsor = entity.sponsor();
        return ProjectSponsorView.builder()
                .projectId(entity.projectId())
                .sponsorId(sponsor.getId())
                .sponsorUrl(sponsor.getUrl())
                .sponsorName(sponsor.getName())
                .sponsorLogoUrl(sponsor.getLogoUrl())
                .lastAllocationDate(entity.lastAllocationDate() != null ?
                        ZonedDateTime.ofInstant(entity.lastAllocationDate().toInstant(), ZoneOffset.UTC) :
                        null)
                .build();
    }
}
