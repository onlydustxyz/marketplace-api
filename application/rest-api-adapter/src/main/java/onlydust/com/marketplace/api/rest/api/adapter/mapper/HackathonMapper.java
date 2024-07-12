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
        return hackathon;
    }

}
