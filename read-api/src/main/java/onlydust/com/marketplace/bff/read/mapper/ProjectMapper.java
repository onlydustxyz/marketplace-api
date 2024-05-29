package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectShortViewEntity;

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
}
