package onlydust.com.marketplace.project.domain.service;

import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TechnologiesServiceTest {
    private final TechnologyStoragePort technologyStoragePort = mock(TechnologyStoragePort.class);
    private final TechnologiesService technologiesService = new TechnologiesService(technologyStoragePort);

    @Test
    public void should_get_all_technologies() {
        when(technologyStoragePort.getAllUsedTechnologies()).thenReturn(List.of("Java", "Kotlin", "Rust"));

        final var technologies = technologiesService.getAllUsedTechnologies();
        assertThat(technologies).containsExactly("Java", "Kotlin", "Rust");
    }
}