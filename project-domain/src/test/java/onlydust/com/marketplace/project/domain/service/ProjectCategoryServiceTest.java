package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProjectCategoryServiceTest {
    Faker faker = new Faker();
    ProjectCategoryService projectCategoryService;
    ProjectObserverPort projectObserverPort = mock(ProjectObserverPort.class);
    PermissionService permissionService = mock(PermissionService.class);
    ProjectCategoryStoragePort projectCategoryStoragePort = mock(ProjectCategoryStoragePort.class);

    @BeforeEach
    void setUp() {
        reset(projectObserverPort, projectCategoryStoragePort, permissionService);
        projectCategoryService = new ProjectCategoryService(projectObserverPort, projectCategoryStoragePort, permissionService);
    }

    @Test
    void cannot_suggest_project_category_if_not_project_lead() {
        // Given
        final var userId = UUID.randomUUID();
        final var projectId = UUID.randomUUID();
        final var name = faker.rickAndMorty().character();

        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> projectCategoryService.suggest(name, userId, projectId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leads can suggest project categories");
    }

    @Test
    void should_suggest_project_category() {
        // Given
        final var userId = UUID.randomUUID();
        final var projectId = UUID.randomUUID();
        final var name = faker.rickAndMorty().character();

        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);

        // When
        projectCategoryService.suggest(name, userId, projectId);

        // Then
        final var suggestionCaptor = ArgumentCaptor.forClass(ProjectCategorySuggestion.class);
        verify(projectCategoryStoragePort).save(suggestionCaptor.capture());
        final var projectCategorySuggestion = suggestionCaptor.getValue();

        assertThat(suggestionCaptor.getValue().id()).isNotNull();
        assertThat(projectCategorySuggestion.name()).isEqualTo(name);
        assertThat(projectCategorySuggestion.projectId()).isEqualTo(projectId);

        verify(projectObserverPort).onProjectCategorySuggested(name, userId);
    }

    @Test
    void should_delete_project_category_suggestion() {
        // Given
        final var suggestionId = ProjectCategorySuggestion.Id.random();

        // When
        projectCategoryService.deleteCategorySuggestion(suggestionId);

        // Then
        verify(projectCategoryStoragePort).delete(suggestionId);
    }

    @Test
    void should_create_project_category() {
        // Given
        final var name = faker.rickAndMorty().character();
        final var iconSlug = faker.rickAndMorty().location();

        // When
        projectCategoryService.createCategory(name, iconSlug);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isNotNull();
        assertThat(projectCategory.name()).isEqualTo(name);
        assertThat(projectCategory.iconSlug()).isEqualTo(iconSlug);
    }

    @Test
    void should_throw_if_not_found() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().location());
        final var newName = faker.rickAndMorty().character();
        final var newIconSlug = faker.rickAndMorty().location();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> projectCategoryService.updateCategory(existing.id(), newName, newIconSlug))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Project category %s not found".formatted(existing.id()));
    }

    @Test
    void should_update_project_category() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().location());
        final var newName = faker.rickAndMorty().character();
        final var newIconSlug = faker.rickAndMorty().location();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));

        // When
        projectCategoryService.updateCategory(existing.id(), newName, newIconSlug);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(newName);
        assertThat(projectCategory.iconSlug()).isEqualTo(newIconSlug);
    }

    @Test
    void should_delete_project_category() {
        // Given
        final var categoryId = ProjectCategory.Id.random();

        // When
        projectCategoryService.deleteCategory(categoryId);

        // Then
        verify(projectCategoryStoragePort).delete(categoryId);
    }
}