package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.ProjectDetailsView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgresProjectAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresProjectAdapter projectStoragePort;
    @Autowired
    UserRepository userRepository;

    @Test
    void should_update_project_more_infos() {
        // Given
        final UUID userId = UUID.randomUUID();
        userRepository.save(new UserEntity(userId, 1L, faker.rickAndMorty().character(),
                faker.rickAndMorty().location(), faker.internet().emailAddress(), new AuthenticatedUser.Role[]{AuthenticatedUser.Role.ADMIN},
                new Date(), new Date(), new Date()));
        final List<NamedLink> namedLinks = List.of(
                NamedLink.builder()
                        .value(faker.pokemon().name())
                        .url(faker.pokemon().location())
                        .build(),
                NamedLink.builder()
                        .value(faker.harryPotter().location())
                        .url(faker.harryPotter().book())
                        .build()
        );
        final UUID projectId = UUID.randomUUID();

        // When
        final String slug = projectStoragePort.createProject(
                projectId, faker.name().lastName(), faker.name().lastName(), faker.name().name(), faker.harryPotter().location(), false,
                namedLinks, null, userId
                , null, ProjectVisibility.PUBLIC, null,
                ProjectRewardSettings.defaultSettings(new Date()), null
        );
        final ProjectDetailsView project = projectStoragePort.getBySlug(slug, null);
        assertEquals(namedLinks.size(), project.getMoreInfos().size());
        assertTrue(project.getMoreInfos().contains(namedLinks.get(0)));
        assertTrue(project.getMoreInfos().contains(namedLinks.get(1)));

        // Then
        final List<NamedLink> namedLinksUpdated = List.of(
                NamedLink.builder()
                        .value(faker.hacker().abbreviation())
                        .url(namedLinks.get(0).getUrl())
                        .build(),
                NamedLink.builder()
                        .value(faker.cat().name())
                        .url(faker.cat().breed())
                        .build()
        );
        projectStoragePort.updateProject(
                projectId, Project.slugOf(project.getName()), project.getName(), project.getShortDescription(), project.getLongDescription(), false,
                namedLinksUpdated,
                null, null, null, null, null, null
        );
        final ProjectDetailsView projectUpdated = projectStoragePort.getBySlug(slug, null);
        assertEquals(namedLinksUpdated.size(), projectUpdated.getMoreInfos().size());
        assertTrue(projectUpdated.getMoreInfos().contains(namedLinksUpdated.get(0)));
        assertTrue(projectUpdated.getMoreInfos().contains(namedLinksUpdated.get(1)));

    }
}
