package com.onlydust.projectmanagement.adapters.secondary.repositories;

import com.onlydust.projectmanagement.hexagon.gateways.repositories.ProjectRepository;
import com.onlydust.projectmanagement.hexagon.models.Project;

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
