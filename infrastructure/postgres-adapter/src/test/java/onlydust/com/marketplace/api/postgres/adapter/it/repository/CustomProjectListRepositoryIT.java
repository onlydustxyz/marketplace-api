package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectListRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class CustomProjectListRepositoryIT extends AbstractPostgresIT {
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectIdRepository projectIdRepository;
    @Autowired
    ProjectRepoRepository projectRepoRepository;
    @Autowired
    CustomProjectListRepository customProjectListRepository;


    @BeforeEach
    void setUp() {
        projectIdRepository.deleteAll();
        projectRepository.deleteAll();
        projectRepoRepository.deleteAll();
    }

    @Test
    void should_return_published_public_projects_with_no_budget_and_no_project_leads() {
        // Given
        final ProjectEntity publicProjectEntity = buildProjectStub(ProjectVisibility.PUBLIC);
        final ProjectEntity privateProjectEntity = buildProjectStub(ProjectVisibility.PRIVATE);

        this.projectIdRepository.save(new ProjectIdEntity(publicProjectEntity.getId()));
        this.projectRepository.save(publicProjectEntity);
        projectRepoRepository.save(new ProjectRepoEntity(publicProjectEntity.getId(), faker.random().nextLong()));

        this.projectIdRepository.save(new ProjectIdEntity(privateProjectEntity.getId()));
        this.projectRepository.save(privateProjectEntity);
        projectRepoRepository.save(new ProjectRepoEntity(privateProjectEntity.getId(), faker.random().nextLong()));

        // When
        final List<ProjectCardView> projects =
                customProjectListRepository.findByTechnologiesSponsorsSearchSortBy(List.of(), List.of(), null,
                        ProjectCardView.SortBy.NAME,0,10);

        // Then
        Assertions.assertEquals(1, projects.size());
        Assertions.assertEquals(publicProjectEntity.getId(), projects.get(0).getId());
    }

    @Test
    void should_return_published_public_projects_with_no_budget_and_no_project_leads_given_a_user_id() {
        final ProjectEntity publicProjectEntity = buildProjectStub(ProjectVisibility.PUBLIC);
        final ProjectEntity privateProjectEntity = buildProjectStub(ProjectVisibility.PRIVATE);

        this.projectIdRepository.save(new ProjectIdEntity(publicProjectEntity.getId()));
        this.projectRepository.save(publicProjectEntity);
        projectRepoRepository.save(new ProjectRepoEntity(publicProjectEntity.getId(), faker.random().nextLong()));

        this.projectIdRepository.save(new ProjectIdEntity(privateProjectEntity.getId()));
        this.projectRepository.save(privateProjectEntity);
        projectRepoRepository.save(new ProjectRepoEntity(privateProjectEntity.getId(), faker.random().nextLong()));

        // When
        final List<ProjectCardView> projects =
                customProjectListRepository.findByTechnologiesSponsorsUserIdSearchSortBy(List.of(), List.of(), null,
                        ProjectCardView.SortBy.NAME, UUID.randomUUID(), false,0,10);

        // Then
        Assertions.assertEquals(1, projects.size());
        Assertions.assertEquals(publicProjectEntity.getId(), projects.get(0).getId());
    }

    @Test
    void should_return_published_public_and_private_projects_with_no_budget_and_no_project_leads_given_a_user_id() {
        final ProjectEntity publicProjectEntity = buildProjectStub(ProjectVisibility.PUBLIC);
        final ProjectEntity privateProjectEntity = buildProjectStub(ProjectVisibility.PRIVATE);

        this.projectIdRepository.save(new ProjectIdEntity(publicProjectEntity.getId()));
        this.projectRepository.save(publicProjectEntity);
        projectRepoRepository.save(new ProjectRepoEntity(publicProjectEntity.getId(), faker.random().nextLong()));

        this.projectIdRepository.save(new ProjectIdEntity(privateProjectEntity.getId()));
        this.projectRepository.save(privateProjectEntity);
        projectRepoRepository.save(new ProjectRepoEntity(privateProjectEntity.getId(), faker.random().nextLong()));

        // When
        final List<ProjectCardView> projects =
                customProjectListRepository.findByTechnologiesSponsorsUserIdSearchSortBy(List.of(), List.of(), null,
                        ProjectCardView.SortBy.NAME, UUID.randomUUID(), false,0,10);

        // Then
        Assertions.assertEquals(1, projects.size());
        Assertions.assertEquals(publicProjectEntity.getId(), projects.get(0).getId());
    }





    private static ProjectEntity buildProjectStub(final ProjectVisibility visibility) {
        return ProjectEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName() + faker.random().nextLong())
                .shortDescription(faker.pokemon().name())
                .longDescription(faker.harryPotter().location())
                .hiring(true)
                .ignoreCodeReviews(false)
                .rank(0)
                .visibility(ProjectMapper.projectVisibilityToEntity(visibility))
                .ignoreIssues(false)
                .ignorePullRequests(false)
                .ignoreCodeReviews(false)
                .build();
    }
}
