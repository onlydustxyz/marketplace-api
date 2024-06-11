package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.UpdateHackathonRequest;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;

import java.util.UUID;

public interface HackathonMapper {

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
