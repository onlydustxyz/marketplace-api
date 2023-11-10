package onlydust.com.marketplace.api.postgres.adapter.entity;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectPageItemViewEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectPageItemViewEntityTest {

    protected static final Faker faker = new Faker();

    @Nested
    public class ShouldReturnSponsorsJsonPath {

        @Test
        void given_no_sponsors() {
            // Given
            final List<String> sponsors = null;

            // When
            final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsors);

            // Then
            assertNull(sponsorsJsonPath);
        }

        @Test
        void given_one_sponsor() {
            // Given
            final String sponsor1 = faker.rickAndMorty().character();
            final List<String> sponsors = List.of(sponsor1);

            // When
            final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsors);

            // Then
            assertEquals("$[*] ? (@.name == \"" + sponsor1 + "\")", sponsorsJsonPath);
        }

        @Test
        void given_two_sponsors() {
            // Given
            final String sponsor1 = faker.rickAndMorty().character();
            final String sponsor2 = faker.pokemon().name();
            final List<String> sponsors = List.of(sponsor1, sponsor2);

            // When
            final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsors);

            // Then
            assertEquals("$[*] ? (@.name == \"" + sponsor1 + "\" || @.name == \"" + sponsor2 + "\")", sponsorsJsonPath);
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
