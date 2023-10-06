package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;

public interface ProjectMapper {

    static ProjectDetailsView mapToProjectDetailsView(ProjectEntity projectEntity) {
        return ProjectDetailsView.builder()
                .id(projectEntity.getId())
                .hiring(projectEntity.getHiring())
                .logoUrl(projectEntity.getLogoUrl())
                .longDescription(projectEntity.getLongDescription())
                .shortDescription(projectEntity.getShortDescription())
                .slug(projectEntity.getKey())
                .name(projectEntity.getName())
                .visibility(mapProjectVisibility(projectEntity.getVisibility()))

                .build();
    }

    static ProjectVisibility mapProjectVisibility(ProjectVisibilityEnumEntity visibility) {
        switch (visibility) {
            case PUBLIC -> {
                return ProjectVisibility.PUBLIC;
            }
            case PRIVATE -> {
                return ProjectVisibility.PRIVATE;
            }
        }
        throw new IllegalArgumentException("Could not map project visibility");
    }
}
