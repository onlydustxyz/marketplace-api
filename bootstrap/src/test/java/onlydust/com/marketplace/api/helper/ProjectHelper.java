package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectHelper {
    @Autowired
    private ProjectFacadePort projectFacadePort;
    @Autowired
    private ProjectStoragePort projectStoragePort;
    private final Faker faker = new Faker();

    public ProjectId create(UserAuthHelper.AuthenticatedUser lead) {
        return ProjectId.of(projectFacadePort.createProject(lead.user().getId(),
                        CreateProjectCommand.builder()
                                .firstProjectLeaderId(lead.user().getId())
                                .name(faker.funnyName().name() + " " + faker.random().nextLong())
                                .shortDescription(faker.lorem().sentence())
                                .longDescription(faker.lorem().paragraph())
                                .isLookingForContributors(faker.bool().bool())
                                .build())
                .getLeft());
    }

    public Project get(ProjectId projectId) {
        return projectStoragePort.getById(projectId.value())
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}
