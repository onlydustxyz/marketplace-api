package onlydust.com.marketplace.api.domain.view;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectCardViewTest {

    @Test
    void should_add_technologies() {
        // Given
        final Map<String, Long> technologies1 = Map.of(
                "Java", 5L, "Rust", 3L
        );
        final Map<String, Long> technologies2 = Map.of(
                "Java", 1L, "Python", 10L
        );
        final ProjectCardView projectCard = ProjectCardView.builder().build();

        // When
        projectCard.addTechnologies(technologies1);
        projectCard.addTechnologies(technologies2);
        final Map<String, Long> technologies = projectCard.getTechnologies();

        // Then
        assertEquals(technologies.get("Java"), 6);
        assertEquals(technologies.get("Rust"), 3);
        assertEquals(technologies.get("Python"), 10);
    }
}