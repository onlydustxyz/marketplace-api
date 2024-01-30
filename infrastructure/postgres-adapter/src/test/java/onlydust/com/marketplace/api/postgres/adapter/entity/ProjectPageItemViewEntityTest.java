package onlydust.com.marketplace.api.postgres.adapter.entity;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectPageItemViewEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectPageItemViewEntityTest {

    protected static final Faker faker = new Faker();

    @Nested
    public class ShouldReturnEcosystemsJsonPath {

        @Test
        void given_no_ecosystems() {
            // Given
            final List<UUID> ecosystems = null;

            // When
            final String ecosystemsJsonPath = ProjectPageItemViewEntity.getEcosystemsJsonPath(ecosystems);

            // Then
            assertNull(ecosystemsJsonPath);
        }

        @Test
        void given_one_ecosystem() {
            // Given
            final UUID ecosystemId1 = UUID.randomUUID();
            final List<UUID> ecosystems = List.of(ecosystemId1);

            // When
            final String ecosystemsJsonPath = ProjectPageItemViewEntity.getEcosystemsJsonPath(ecosystems);

            // Then
            assertEquals("$[*] ? (@.id == \"" + ecosystemId1 + "\")", ecosystemsJsonPath);
        }

        @Test
        void given_two_ecosystems() {
            // Given
            final UUID ecosystemId1 = UUID.randomUUID();
            final UUID ecosystemId2 = UUID.randomUUID();
            final List<UUID> ecosystemIds = List.of(ecosystemId1, ecosystemId2);

            // When
            final String ecosystemsJsonPath = ProjectPageItemViewEntity.getEcosystemsJsonPath(ecosystemIds);

            // Then
            assertEquals("$[*] ? (@.id == \"" + ecosystemId1 + "\" || @.id == \"" + ecosystemId2 + "\")", ecosystemsJsonPath);
        }


    }

    @Nested
    public class ShouldReturnTagsJsonPath {

        @Test
        void given_no_tags() {
            // Given
            final List<Project.Tag> tags = null;

            // When
            final String tagsJsonPath = ProjectPageItemViewEntity.getTagsJsonPath(tags);

            // Then
            assertNull(tagsJsonPath);
        }

        @Test
        void given_one_tag() {
            // Given
            final List<Project.Tag> tags = List.of(Project.Tag.BEGINNERS_WELCOME);

            // When
            final String tagsJsonPath = ProjectPageItemViewEntity.getTagsJsonPath(tags);

            // Then
            assertEquals("$[*] ? (@.name == \"BEGINNERS_WELCOME\")", tagsJsonPath);
        }

        @Test
        void given_two_tags() {
            // Given
            final List<Project.Tag> tags = List.of(Project.Tag.BEGINNERS_WELCOME, Project.Tag.STRONG_EXPERTISE);

            // When
            final String tagsJsonPath = ProjectPageItemViewEntity.getTagsJsonPath(tags);

            // Then
            assertEquals("$[*] ? (@.name == \"BEGINNERS_WELCOME\" || @.name == \"STRONG_EXPERTISE\")", tagsJsonPath);
        }


    }

    @Nested
    public class ShouldReturnTechnologiesJsonPath {


        @Test
        void given_no_technologies() {
            // Given
            final List<String> technologies = null;

            // When
            final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);

            // Then
            assertNull(technologiesJsonPath);
        }

        @Test
        void given_one_technology() {
            // Given
            final String technology1 = faker.harryPotter().character();
            final List<String> technologies = List.of(technology1);

            // When
            final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);

            // Then
            assertEquals("$[*] ? (@.\"" + technology1 + "\" > 0)", technologiesJsonPath);
        }

        @Test
        void given_two_technologies() {
            // Given
            final String technology1 = faker.harryPotter().character();
            final String technology2 = faker.harryPotter().location();
            final List<String> technologies = List.of(technology1, technology2);

            // When
            final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);

            // Then
            assertEquals("$[*] ? (@.\"" + technology1 + "\" > 0 || @.\"" + technology2 + "\" > 0)", technologiesJsonPath);
        }


    }

}
