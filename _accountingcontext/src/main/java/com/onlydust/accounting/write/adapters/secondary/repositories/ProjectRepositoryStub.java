package com.onlydust.accounting.write.adapters.secondary.repositories;

import com.onlydust.accounting.write.hexagon.gateways.repositories.ProjectRepository;
import com.onlydust.accounting.write.hexagon.models.Project;

import java.util.*;

public class ProjectRepositoryStub implements ProjectRepository {

    private Map<UUID, Project> projectsByIds = new HashMap<>();

    @Override
    public Optional<Project> byId(UUID projectId) {
        return projectsByIds.containsKey(projectId) ?
                Optional.of(projectsByIds.get(projectId)) :
                Optional.empty();
    }

    public void setProjectsByIds(Map<UUID, Project> projectsByIds) {
        this.projectsByIds = projectsByIds;
    }

    public Collection<Project> projects() {
        return projectsByIds.values();
    }
}
