package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectShortViewEntity;

import java.util.Objects;

public interface ProjectMapper {
    static ProjectLinkResponse map(final @NonNull ProjectLinkViewEntity projectLink) {
        return new ProjectLinkResponse()
                .id(projectLink.id())
                .slug(projectLink.slug())
                .name(projectLink.name())
                .logoUrl(projectLink.logoUrl());
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

    public enum SortBy {
        CONTRIBUTORS_COUNT, REPOS_COUNT, RANK, NAME;
    }

    static SortBy mapSortByParameter(final String sort) {
        if (Objects.nonNull(sort)) {
            if (sort.equals("RANK")) {
                return SortBy.RANK;
            }
            if (sort.equals("NAME")) {
                return SortBy.NAME;
            }
            if (sort.equals("REPO_COUNT")) {
                return SortBy.REPOS_COUNT;
            }
            if (sort.equals("CONTRIBUTOR_COUNT")) {
                return SortBy.CONTRIBUTORS_COUNT;
            }
        }
        return null;
    }
}
