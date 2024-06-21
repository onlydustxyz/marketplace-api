package onlydust.com.marketplace.api.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectShortViewEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;

public interface ProjectMapper {
    static ProjectLinkResponse map(final @NonNull ProjectLinkViewEntity projectLink) {
        return new ProjectLinkResponse()
                .id(projectLink.id())
                .slug(projectLink.slug())
                .name(projectLink.name())
                .logoUrl(projectLink.logoUrl());
    }

    static ProjectLinkResponse map(final @NonNull PublicProjectReadEntity project) {
        return new ProjectLinkResponse()
                .id(project.getId())
                .slug(project.getSlug())
                .name(project.getName())
                .logoUrl(project.getLogoUrl());
    }

    static onlydust.com.backoffice.api.contract.model.ProjectLinkResponse mapBO(final @NonNull ProjectLinkViewEntity projectLink) {
        return new onlydust.com.backoffice.api.contract.model.ProjectLinkResponse()
                .id(projectLink.id())
                .slug(projectLink.slug())
                .name(projectLink.name())
                .logoUrl(projectLink.logoUrl());
    }

    static ProjectShortResponse projectToResponse(final ProjectShortViewEntity projectLink) {
        return new ProjectShortResponse()
                .id(projectLink.id())
                .logoUrl(projectLink.logoUrl())
                .shortDescription(projectLink.shortDescription())
                .slug(projectLink.slug())
                .name(projectLink.name());
    }

    enum SortBy {
        CONTRIBUTORS_COUNT, REPOS_COUNT, RANK, NAME;
    }

    static SortBy mapSortByParameter(final String sort) {
        return sort == null ? null : switch (sort) {
            case "RANK" -> SortBy.RANK;
            case "NAME" -> SortBy.NAME;
            case "REPO_COUNT" -> SortBy.REPOS_COUNT;
            case "CONTRIBUTOR_COUNT" -> SortBy.CONTRIBUTORS_COUNT;
            default -> null;
        };
    }
}
