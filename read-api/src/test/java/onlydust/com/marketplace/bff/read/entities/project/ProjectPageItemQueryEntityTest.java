package onlydust.com.marketplace.api.postgres.adapter.entity;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.bff.read.entities.project.ProjectPageItemQueryEntity;
import onlydust.com.marketplace.project.domain.model.Project;
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
    public class ShouldReturnLanguagesJsonPath {

        @Test
        void given_no_languages() {
            // Given
            final List<UUID> languageIds = null;

            // When
            final String languagesJsonPath = ProjectPageItemQueryEntity.getLanguagesJsonPath(languageIds);

            // Then
            assertNull(languagesJsonPath);
        }

        @Test
        void given_one_language() {
            // Given
            final List<UUID> languageIds = List.of(UUID.randomUUID());

            // When
            final String languagesJsonPath = ProjectPageItemQueryEntity.getLanguagesJsonPath(languageIds);

            // Then
            assertEquals("$[*] ? (@.id == \"" + languageIds.get(0) + "\")", languagesJsonPath);
        }

        @Test
        void given_two_languages() {
            // Given
            final List<UUID> languageIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            // When
            final String languagesJsonPath = ProjectPageItemQueryEntity.getLanguagesJsonPath(languageIds);

            // Then
            assertEquals("$[*] ? (@.id == \"" + languageIds.get(0) + "\" || @.id == \"" + languageIds.get(1) + "\")", languagesJsonPath);
        }

    }

}
