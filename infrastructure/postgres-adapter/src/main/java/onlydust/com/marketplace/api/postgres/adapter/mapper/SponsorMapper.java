package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectSponsorViewEntity;
import onlydust.com.marketplace.project.domain.view.ProjectSponsorView;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public interface SponsorMapper {

    static ProjectSponsorView mapToSponsor(final ProjectSponsorViewEntity entity) {
        final var sponsor = entity.sponsor();
        final var project = entity.project();
        return ProjectSponsorView.builder()
                .projectId(entity.projectId())
                .sponsorId(sponsor.getId())
                .sponsorUrl(sponsor.getUrl())
                .sponsorName(sponsor.getName())
                .sponsorLogoUrl(sponsor.getLogoUrl())
                .project(project.toDomain())
                .lastAllocationDate(entity.lastAllocationDate() != null ?
                        ZonedDateTime.ofInstant(entity.lastAllocationDate().toInstant(), ZoneOffset.UTC) :
                        null)
                .build();
    }
}
