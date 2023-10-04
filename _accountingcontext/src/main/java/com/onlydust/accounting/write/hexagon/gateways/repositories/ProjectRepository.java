package com.onlydust.accounting.write.hexagon.gateways.repositories;

import com.onlydust.accounting.write.hexagon.models.Project;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    Optional<Project> byId(UUID projectId);

}
