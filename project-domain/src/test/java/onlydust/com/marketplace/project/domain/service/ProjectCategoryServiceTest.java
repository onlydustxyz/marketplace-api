package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ProjectCategoryServiceTest {
    Faker faker = new Faker();
    ProjectCategoryService projectCategoryService;
    ProjectObserverPort projectObserverPort = mock(ProjectObserverPort.class);
    ProjectCategoryStoragePort projectCategoryStoragePort = mock(ProjectCategoryStoragePort.class);

    @BeforeEach
    void setUp() {
        reset(projectObserverPort, projectCategoryStoragePort);
        projectCategoryService = new ProjectCategoryService(projectObserverPort, projectCategoryStoragePort);
    }

    @Test
    void should_suggest_project_category() {
        // Given
        final String projectCategoryName = faker.rickAndMorty().character();
        final UUID userId = UUID.randomUUID();

        // When
        projectCategoryService.suggest(projectCategoryName, userId);

        // Then
        final var projectCategoryArgumentCaptor = ArgumentCaptor.forClass(ProjectCategorySuggestion.class);
        verify(projectCategoryStoragePort).save(projectCategoryArgumentCaptor.capture());
        assertEquals(projectCategoryName, projectCategoryArgumentCaptor.getValue().name());
        assertNotNull(projectCategoryArgumentCaptor.getValue().id());
        verify(projectObserverPort).onProjectCategorySuggested(projectCategoryName, userId);
    }
}