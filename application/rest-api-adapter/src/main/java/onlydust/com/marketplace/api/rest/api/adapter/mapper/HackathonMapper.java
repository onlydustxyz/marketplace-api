package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.UpdateHackathonRequest;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;

import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public interface HackathonMapper {

    static Hackathon toDomain(UUID hackathonId, UpdateHackathonRequest request) {
        final var hackathon = Hackathon.builder()
                .id(Hackathon.Id.of(hackathonId))
                .status(Hackathon.Status.valueOf(request.getStatus().name()))
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .totalBudget(request.getTotalBudget())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        hackathon.githubLabels().addAll(request.getGithubLabels());

        hackathon.communityLinks().addAll(request.getCommunityLinks().stream().map(link -> NamedLink.builder()
                .url(link.getUrl())
                .value(link.getValue())
                .build()).toList());

        hackathon.links().addAll(request.getLinks().stream().map(link -> NamedLink.builder()
                .url(link.getUrl())
                .value(link.getValue())
                .build()).toList());

        hackathon.sponsorIds().addAll(request.getSponsorIds());
        hackathon.projectIds().addAll(request.getProjectIds());
        hackathon.events().addAll(request.getEvents().stream().map(event -> Hackathon.Event.builder()
                .id(isNull(event.getId()) ? UUID.randomUUID() : event.getId())
                .name(event.getName())
                .subtitle(event.getSubtitle())
                .iconSlug(event.getIconSlug())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .links(event.getLinks().stream().map(link -> NamedLink.builder()
                        .url(link.getUrl())
                        .value(link.getValue())
                        .build()).collect(Collectors.toSet()))
                .build()).toList());
        return hackathon;
    }

}
