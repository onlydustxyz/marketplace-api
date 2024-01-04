package onlydust.com.marketplace.api.postgres.adapter.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectPageItemViewEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProjectPageItemViewEntityTest {

  protected static final Faker faker = new Faker();

  @Nested
  public class ShouldReturnSponsorsJsonPath {

    @Test
    void given_no_sponsors() {
      // Given
      final List<UUID> sponsors = null;

      // When
      final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsors);

      // Then
      assertNull(sponsorsJsonPath);
    }

    @Test
    void given_one_sponsor() {
      // Given
      final UUID sponsorId1 = UUID.randomUUID();
      final List<UUID> sponsors = List.of(sponsorId1);

      // When
      final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsors);

      // Then
      assertEquals("$[*] ? (@.id == \"" + sponsorId1 + "\")", sponsorsJsonPath);
    }

    @Test
    void given_two_sponsors() {
      // Given
      final UUID sponsorId1 = UUID.randomUUID();
      final UUID sponsorId2 = UUID.randomUUID();
      final List<UUID> sponsorIds = List.of(sponsorId1, sponsorId2);

      // When
      final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsorIds);

      // Then
      assertEquals("$[*] ? (@.id == \"" + sponsorId1 + "\" || @.id == \"" + sponsorId2 + "\")", sponsorsJsonPath);
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
