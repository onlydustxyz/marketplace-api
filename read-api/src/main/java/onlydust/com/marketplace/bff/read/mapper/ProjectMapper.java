package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;

public interface ProjectMapper {
    static ProjectLinkResponse map(final @NonNull ProjectLinkViewEntity projectLink) {
        return new ProjectLinkResponse()
                .id(projectLink.id())
                .slug(projectLink.slug())
                .name(projectLink.name())
                .logoUrl(projectLink.logoUrl());
    }
}
