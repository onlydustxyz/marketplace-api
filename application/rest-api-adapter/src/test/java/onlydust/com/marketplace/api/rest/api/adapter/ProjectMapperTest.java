package onlydust.com.marketplace.api.rest.api.adapter;

import onlydust.com.marketplace.api.domain.view.RepositoryView;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.repositoriesToTechnologies;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectMapperTest {

    @Test
    void should_map_technologies() {
        // Given
        final RepositoryView repositoryView1 = RepositoryView.builder()
                .technologies(Map.of(
                        "Java", 5, "Rust", 3
                ))
                .build();
        final RepositoryView repositoryView2 = RepositoryView.builder()
                .technologies(Map.of(
                        "Java", 1, "Python", 10
                ))
                .build();
        final Set<String> technologiesNames = new HashSet<>();

        // When
        final Map<String, Integer> technologies = repositoriesToTechnologies(List.of(repositoryView1,
                repositoryView2), technologiesNames);

        // Then
        assertEquals(technologies.get("Java"), 6);
        assertEquals(technologies.get("Rust"), 3);
        assertEquals(technologies.get("Python"), 10);
        assertTrue(technologiesNames.contains("Java"));
        assertTrue(technologiesNames.contains("Rust"));
        assertTrue(technologiesNames.contains("Python"));
    }
}
