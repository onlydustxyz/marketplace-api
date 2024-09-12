package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectMoreInfoEntity;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.Project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ProjectMapper {

    static Project mapShortProjectViewToProject(ShortProjectQueryEntity project) {
        return Project.builder()
                .id(ProjectId.of(project.getId()))
                .slug(project.getSlug())
                .name(project.getName())
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .logoUrl(project.getLogoUrl())
                .hiring(project.getHiring())
                .visibility(project.getVisibility())
                .build();
    }

    static Set<ProjectMoreInfoEntity> moreInfosToEntities(final List<NamedLink> moreInfos, final ProjectId projectId) {
        final Set<ProjectMoreInfoEntity> entities = new HashSet<>();
        for (int i = 0; i < moreInfos.size(); i++) {
            final var moreInfo = moreInfos.get(i);
            entities.add(ProjectMoreInfoEntity.builder()
                    .projectId(projectId.value())
                    .url(moreInfo.getUrl())
                    .name(moreInfo.getValue())
                    .rank(i)
                    .build());
        }
        return entities;
    }
}
