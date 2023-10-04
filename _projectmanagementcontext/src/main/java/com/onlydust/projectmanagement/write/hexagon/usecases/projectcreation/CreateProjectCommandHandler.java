package com.onlydust.projectmanagement.write.hexagon.usecases.projectcreation;

import com.onlydust.projectmanagement.write.adapters.secondary.repositories.ProjectRepositoryStub;
import com.onlydust.projectmanagement.write.hexagon.gateways.repositories.ProjectRepository;
import com.onlydust.projectmanagement.write.hexagon.models.Project;
import com.onlydust.shared.write.hexagon.gateways.uuidgeneration.UuidGenerator;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CreateProjectCommandHandler {

    private final ProjectRepository projectRepository;
    private final UuidGenerator uuidGenerator;

    public CreateProjectCommandHandler(ProjectRepositoryStub projectRepository,
                                       UuidGenerator uuidGenerator) {

        this.projectRepository = projectRepository;
        this.uuidGenerator = uuidGenerator;
    }

    public void handle(CreateProjectCommand createProjectCommand) {
        projectRepository.save(new Project(
                uuidGenerator.generate(),
                createProjectCommand.name()
        ));
    }

}
