package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;

import java.util.UUID;

public interface HackathonMapper {
    static HackathonsDetailsResponse toBackOfficeResponse(HackathonDetailsView view) {
        return new HackathonsDetailsResponse()
                .id(view.id().value())
                .slug(view.slug())
                .status(switch (view.status()) {
                    case DRAFT -> HackathonStatus.DRAFT;
                    case PUBLISHED -> HackathonStatus.PUBLISHED;
                })
                .title(view.title())
                .subtitle(view.subtitle())
                .description(view.description())
                .location(view.location())
                .totalBudget(view.totalBudget())
                .startDate(view.startDate())
                .endDate(view.endDate())
                .links(view.links().stream().map(link -> new SimpleLink()
                        .value(link.getValue())
                        .url(link.getUrl())
                ).toList())
                .sponsors(view.sponsors().stream().map(sponsor -> new SponsorResponse()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                ).toList())
                .tracks(view.tracks().stream().map(track -> new HackathonsTrackResponse()
                        .name(track.name())
                        .subtitle(track.subtitle())
                        .description(track.description())
                        .iconSlug(track.iconSlug())
                        .projects(track.projects().stream().map(project -> new ProjectLinkResponse()
                                .id(project.id())
                                .slug(project.slug())
                                .name(project.name())
                                .logoUrl(project.logoUrl())
                        ).toList())
                ).toList());
    }

    static Hackathon toDomain(UUID hackathonId, UpdateHackathonRequest request) {
        final var hackathon = Hackathon.builder()
                .id(Hackathon.Id.of(hackathonId))
                .status(Hackathon.Status.valueOf(request.getStatus().name()))
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .totalBudget(request.getTotalBudget())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        hackathon.links().addAll(request.getLinks().stream().map(link -> NamedLink.builder()
                .url(link.getUrl())
                .value(link.getValue())
                .build()).toList());
        
        hackathon.sponsorIds().addAll(request.getSponsorIds());

        hackathon.tracks().addAll(request.getTracks().stream().map(track -> new Hackathon.Track(
                track.getName(),
                track.getSubtitle(),
                track.getDescription(),
                track.getIconSlug(),
                track.getProjectIds()
        )).toList());
        return hackathon;
    }
}
