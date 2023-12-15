package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.domain.model.MoreInfoLink;
import onlydust.com.marketplace.api.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgresProjectAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresProjectAdapter postgresProjectAdapter;
    @Autowired
    UserRepository userRepository;

    @Test
    void should_update_project_more_infos() {
        // Given
        final UUID userId = UUID.randomUUID();
        userRepository.save(new UserEntity(userId, 1L, faker.rickAndMorty().character(),
                faker.rickAndMorty().location(), faker.internet().emailAddress(), new UserRole[]{UserRole.ADMIN},
                new Date(), new Date(), new Date()));
        final List<MoreInfoLink> moreInfoLinks = List.of(
                MoreInfoLink.builder()
                        .value(faker.pokemon().name())
                        .url(faker.pokemon().location())
                        .build(),
                MoreInfoLink.builder()
                        .value(faker.harryPotter().location())
                        .url(faker.harryPotter().book())
                        .build()
        );
        final UUID projectId = UUID.randomUUID();

        // When
        final String slug = postgresProjectAdapter.createProject(
                projectId, faker.name().lastName(), faker.name().name(), faker.harryPotter().location(), false,
                moreInfoLinks, null, userId
                , null, ProjectVisibility.PUBLIC, null,
                ProjectRewardSettings.defaultSettings(new Date())
        );
        final ProjectDetailsView project = postgresProjectAdapter.getBySlug(slug, null);
        assertEquals(moreInfoLinks.size(), project.getMoreInfos().size());
        assertTrue(project.getMoreInfos().contains(moreInfoLinks.get(0)));
        assertTrue(project.getMoreInfos().contains(moreInfoLinks.get(1)));

        // Then
        final List<MoreInfoLink> moreInfoLinksUpdated = List.of(
                MoreInfoLink.builder()
                        .value(faker.hacker().abbreviation())
                        .url(moreInfoLinks.get(0).getUrl())
                        .build(),
                MoreInfoLink.builder()
                        .value(faker.cat().name())
                        .url(faker.cat().breed())
                        .build()
        );
        postgresProjectAdapter.updateProject(
                projectId, project.getName(), project.getShortDescription(), project.getLongDescription(), false,
                moreInfoLinksUpdated,
                null, null, null, null, null
        );
        final ProjectDetailsView projectUpdated = postgresProjectAdapter.getBySlug(slug, null);
        assertEquals(moreInfoLinksUpdated.size(), projectUpdated.getMoreInfos().size());
        assertTrue(projectUpdated.getMoreInfos().contains(moreInfoLinksUpdated.get(0)));
        assertTrue(projectUpdated.getMoreInfos().contains(moreInfoLinksUpdated.get(1)));

    }
}
