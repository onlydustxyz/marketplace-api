package com.onlydust.projectmanagement.hexagon.usecases.projectcreation;

import com.onlydust.projectmanagement.adapters.secondary.repositories.ProjectRepositoryStub;
import com.onlydust.projectmanagement.hexagon.gateways.uuidgeneration.UuidGenerator;
import com.onlydust.projectmanagement.hexagon.models.Budget;
import com.onlydust.projectmanagement.hexagon.models.Project;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CreateProjectCommandHandler {

    private final ProjectRepositoryStub projectRepository;
    private final UuidGenerator uuidGenerator;

    public CreateProjectCommandHandler(ProjectRepositoryStub projectRepository,
                                       UuidGenerator uuidGenerator) {

        this.projectRepository = projectRepository;
        this.uuidGenerator = uuidGenerator;
    }

    public void handle(CreateProjectCommand createProjectCommand) {
        projectRepository.save(new Project(
                uuidGenerator.generate(),
                createProjectCommand.name(),
                new Budget(createProjectCommand.amount(), createProjectCommand.currency())
        ));
    }

}
