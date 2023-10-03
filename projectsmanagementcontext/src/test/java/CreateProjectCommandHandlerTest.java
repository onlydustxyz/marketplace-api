import com.onlydust.projectmanagement.adapters.secondary.repositories.ProjectRepositoryStub;
import com.onlydust.projectmanagement.adapters.secondary.uuidgeneration.DeterministicUuidGenerator;
import com.onlydust.projectmanagement.hexagon.models.Budget;
import com.onlydust.projectmanagement.hexagon.models.Project;
import com.onlydust.projectmanagement.hexagon.usecases.projectcreation.CreateProjectCommand;
import com.onlydust.projectmanagement.hexagon.usecases.projectcreation.CreateProjectCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateProjectCommandHandlerTest {

    private final ProjectRepositoryStub projectRepository = new ProjectRepositoryStub();
    private final DeterministicUuidGenerator uuidGenerator = new DeterministicUuidGenerator();

    private final UUID aProjectId = UUID.fromString("f7f6e5d4-c3b2-11eb-b8bc-0242ac130003");

    @BeforeEach
    public void setup() {
        uuidGenerator.setNextUuid(aProjectId);
    }

    @Test
    void should_create_project() {
        var createProjectCommand = new CreateProjectCommand(
                "FP-TS-2",
                1000,
                "EUR"
        );

        new CreateProjectCommandHandler(projectRepository, uuidGenerator).handle(createProjectCommand);

        assertThat(projectRepository.projects()).containsExactly(new Project(
                aProjectId,
                "FP-TS-2",
                new Budget(1000, "EUR")
        ));
    }

}
