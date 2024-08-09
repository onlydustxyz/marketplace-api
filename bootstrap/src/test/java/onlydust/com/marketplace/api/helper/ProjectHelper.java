package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectHelper {
    @Autowired
    private ProjectFacadePort projectFacadePort;
    private final Faker faker = new Faker();

    public ProjectId create(UserAuthHelper.AuthenticatedUser lead) {
        return ProjectId.of(projectFacadePort.createProject(lead.user().getId(),
                        CreateProjectCommand.builder()
                                .firstProjectLeaderId(lead.user().getId())
                                .name(faker.funnyName().name())
                                .shortDescription(faker.lorem().sentence())
                                .longDescription(faker.lorem().paragraph())
                                .isLookingForContributors(faker.bool().bool())
                                .build())
                .getLeft());
    }
}
