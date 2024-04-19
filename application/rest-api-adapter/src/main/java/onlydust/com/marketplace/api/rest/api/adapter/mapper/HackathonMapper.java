package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;

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
}
