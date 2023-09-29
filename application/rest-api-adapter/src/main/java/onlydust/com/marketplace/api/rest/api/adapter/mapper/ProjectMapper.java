package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ShortProjectResponse;
import onlydust.com.marketplace.api.domain.model.Project;

public interface ProjectMapper {

    static ShortProjectResponse projectToResponse(Project project) {
        final ShortProjectResponse projectResponse = new ShortProjectResponse();
        projectResponse.setId(project.getId());
        projectResponse.setName(project.getName());
        projectResponse.setLogoUrl(project.getLogoUrl());
        projectResponse.setShortDescription(project.getShortDescription());
        projectResponse.setPrettyId(project.getSlug());
        projectResponse.setVisibility(ShortProjectResponse.VisibilityEnum.PUBLIC);
        return projectResponse;
    }
}
