package onlydust.com.marketplace.api.postgres.adapter.entity;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectPageItemQueryEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectPageItemQueryEntityTest {

    protected static final Faker faker = new Faker();

    @Nested
    public class ShouldReturnEcosystemsJsonPath {

        @Test
        void given_no_ecosystems() {
            // Given
            List<String> ecosystemSlugs = null;

            // When
            final String ecosystemsJsonPath = ProjectPageItemQueryEntity.getEcosystemsJsonPath(ecosystemSlugs);

            // Then
            assertNull(ecosystemsJsonPath);
        }

        @Test
        void given_one_ecosystem() {
            // Given
            List<String> ecosystemSlugs = List.of("slug1");

            // When
            final String ecosystemsJsonPath = ProjectPageItemQueryEntity.getEcosystemsJsonPath(ecosystemSlugs);

            // Then
            assertEquals("$[*] ? (@.slug == \"" + "slug1" + "\")", ecosystemsJsonPath);
        }

        @Test
        void given_two_ecosystems() {
            // Given
            final String slug1 = "slug1";
            final String slug2 = "slug2";
            List<String> ecosystemSlugs = List.of(slug1, slug2);

            // When
            final String ecosystemsJsonPath = ProjectPageItemQueryEntity.getEcosystemsJsonPath(ecosystemSlugs);

            // Then
            assertEquals("$[*] ? (@.slug == \"" + slug1 + "\" || @.slug == \"" + slug2 + "\")", ecosystemsJsonPath);
        }


    }

    @Nested
    public class ShouldReturnTagsJsonPath {

        @Test
        void given_no_tags() {
            // Given
            final List<String> tags = null;

            // When
            final String tagsJsonPath = ProjectPageItemQueryEntity.getTagsJsonPath(tags);

            // Then
            assertNull(tagsJsonPath);
        }

        @Test
        void given_one_tag() {
            // Given
            final List<String> tags = Stream.of(Project.Tag.NEWBIES_WELCOME).map(Enum::name).toList();

            // When
            final String tagsJsonPath = ProjectPageItemQueryEntity.getTagsJsonPath(tags);

            // Then
            assertEquals("$[*] ? (@.name == \"NEWBIES_WELCOME\")", tagsJsonPath);
        }

        @Test
        void given_two_tags() {
            // Given
            final List<String> tags = Stream.of(Project.Tag.LIKELY_TO_REWARD, Project.Tag.FAST_AND_FURIOUS).map(Enum::name).toList();

            // When
            final String tagsJsonPath = ProjectPageItemQueryEntity.getTagsJsonPath(tags);

            // Then
            assertEquals("$[*] ? (@.name == \"LIKELY_TO_REWARD\" || @.name == \"FAST_AND_FURIOUS\")", tagsJsonPath);
        }


    }

    @Nested
    public class ShouldReturnTechnologiesJsonPath {


        @Test
        void given_no_technologies() {
            // Given
            final List<String> technologies = null;

            // When
            final String technologiesJsonPath = ProjectPageItemQueryEntity.getTechnologiesJsonPath(technologies);

            // Then
            assertNull(technologiesJsonPath);
        }

        @Test
        void given_one_technology() {
            // Given
            final String technology1 = faker.harryPotter().character();
            final List<String> technologies = List.of(technology1);

            // When
            final String technologiesJsonPath = ProjectPageItemQueryEntity.getTechnologiesJsonPath(technologies);

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
            final String technologiesJsonPath = ProjectPageItemQueryEntity.getTechnologiesJsonPath(technologies);

            // Then
            assertEquals("$[*] ? (@.\"" + technology1 + "\" > 0 || @.\"" + technology2 + "\" > 0)", technologiesJsonPath);
        }


    }

}
