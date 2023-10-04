package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;

import java.util.UUID;

public class Project extends AggregateRoot<ProjectId> {

    public Project(ProjectId id) {
        super(id);
    }
}
