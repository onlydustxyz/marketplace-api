package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.DatabaseHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectGlobalDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsV2PageApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectViewRepository projectViewRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setUp() {
        final var categoryAI = new ProjectCategoryEntity(UUID.fromString("b151c7e4-1493-4927-bb0f-8647ec98a9c5"), "ai", "AI", "AI is cool", "brain", Set.of());
        projectCategoryRepository.saveAll(List.of(
                new ProjectCategoryEntity(UUID.fromString("7a1c0dcb-2079-487c-adaa-88d425bf13ea"), "security", "Security", "Security is important", "lock",
                        Set.of()),
                categoryAI
        ));
        final var project = projectRepository.findById(UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c")).get();
        project.setCategories(Set.of(new ProjectProjectCategoryEntity(project.getId(), categoryAI.getId())));
        projectRepository.saveAndFlush(project);
        databaseHelper.executeInTransaction(() -> biProjectGlobalDataRepository.refreshByProject(ProjectId.of(project.getId())));
    }


    @Test
    void should_get_projects_given_filters() {
        // Given

        // When
        client.get()
                .uri(getApiURI(PROJECTS_V2_GET))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(System.out::println);
        // Then
    }
}
