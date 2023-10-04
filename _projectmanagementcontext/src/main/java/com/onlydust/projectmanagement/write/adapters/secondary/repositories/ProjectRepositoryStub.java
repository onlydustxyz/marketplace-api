package com.onlydust.projectmanagement.write.adapters.secondary.repositories;

import com.onlydust.projectmanagement.write.hexagon.gateways.repositories.ProjectRepository;
import com.onlydust.projectmanagement.write.hexagon.models.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepositoryStub implements ProjectRepository {

    private List<Project> projects = new ArrayList<>();

    @Override
    public void save(Project project) {
        projects.add(project);
    }

    public List<Project> projects() {
        return projects;
    }
}
