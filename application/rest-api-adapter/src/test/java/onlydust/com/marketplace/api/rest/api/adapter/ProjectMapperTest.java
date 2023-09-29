package onlydust.com.marketplace.api.rest.api.adapter;

import onlydust.com.marketplace.api.domain.view.RepositoryView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.repositoriesToTechnologies;

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

        // When
        final Map<String, Integer> technologies = repositoriesToTechnologies(List.of(repositoryView1,
                repositoryView2));

        // Then
        Assertions.assertEquals(technologies.get("Java"), 6);
        Assertions.assertEquals(technologies.get("Rust"), 3);
        Assertions.assertEquals(technologies.get("Python"), 10);
    }
}
