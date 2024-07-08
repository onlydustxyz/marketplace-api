package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
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
    ProjectCategoryStoragePort projectCategoryStoragePort = mock(ProjectCategoryStoragePort.class);

    @BeforeEach
    void setUp() {
        reset(projectCategoryStoragePort);
        projectCategoryService = new ProjectCategoryService(projectCategoryStoragePort);
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
        final var description = faker.rickAndMorty().quote();
        final var iconSlug = faker.rickAndMorty().location();

        // When
        projectCategoryService.createCategory(name, description, iconSlug, null);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isNotNull();
        assertThat(projectCategory.name()).isEqualTo(name);
        assertThat(projectCategory.description()).isEqualTo(description);
        assertThat(projectCategory.iconSlug()).isEqualTo(iconSlug);
    }

    @Test
    void should_create_project_category_from_a_suggestion() {
        // Given
        final var name = faker.rickAndMorty().character();
        final var description = faker.rickAndMorty().quote();
        final var iconSlug = faker.rickAndMorty().location();
        final var suggestionId = ProjectCategorySuggestion.Id.random();
        final var projectId = UUID.randomUUID();

        when(projectCategoryStoragePort.get(suggestionId)).thenReturn(Optional.of(ProjectCategorySuggestion.of(name, projectId)));

        // When
        projectCategoryService.createCategory(name, description, iconSlug, suggestionId);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isNotNull();
        assertThat(projectCategory.name()).isEqualTo(name);
        assertThat(projectCategory.description()).isEqualTo(description);
        assertThat(projectCategory.iconSlug()).isEqualTo(iconSlug);
        assertThat(projectCategory.projects()).contains(projectId);

        verify(projectCategoryStoragePort).delete(suggestionId);
    }

    @Test
    void should_fail_to_create_category_if_suggestion_not_found() {
        // Given
        final var name = faker.rickAndMorty().character();
        final var description = faker.rickAndMorty().quote();
        final var iconSlug = faker.rickAndMorty().location();
        final var suggestionId = ProjectCategorySuggestion.Id.random();

        when(projectCategoryStoragePort.get(suggestionId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> projectCategoryService.createCategory(name, description, iconSlug, suggestionId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Project category suggestion %s not found".formatted(suggestionId));

        verify(projectCategoryStoragePort, never()).delete(suggestionId);
    }

    @Test
    void should_throw_if_not_found() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        final var newName = faker.rickAndMorty().character();
        final var newDescription = faker.rickAndMorty().quote();
        final var newIconSlug = faker.rickAndMorty().location();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> projectCategoryService.updateCategory(existing.id(), newName, newDescription, newIconSlug, null))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Project category %s not found".formatted(existing.id()));
    }

    @Test
    void should_update_project_category() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        final var newName = faker.rickAndMorty().character();
        final var newDescription = faker.rickAndMorty().quote();
        final var newIconSlug = faker.rickAndMorty().location();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));

        // When
        projectCategoryService.updateCategory(existing.id(), newName, newDescription, newIconSlug, null);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(newName);
        assertThat(projectCategory.description()).isEqualTo(newDescription);
        assertThat(projectCategory.iconSlug()).isEqualTo(newIconSlug);
    }

    @Test
    void should_partial_update_project_category_name() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        final var newName = faker.rickAndMorty().character();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));

        // When
        projectCategoryService.updateCategory(existing.id(), newName, null, null, null);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(newName);
        assertThat(projectCategory.description()).isEqualTo(existing.description());
        assertThat(projectCategory.iconSlug()).isEqualTo(existing.iconSlug());
    }

    @Test
    void should_partial_update_project_category_description() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        final var newDescription = faker.rickAndMorty().quote();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));

        // When
        projectCategoryService.updateCategory(existing.id(), null, newDescription, null, null);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(existing.name());
        assertThat(projectCategory.description()).isEqualTo(newDescription);
        assertThat(projectCategory.iconSlug()).isEqualTo(existing.iconSlug());
    }

    @Test
    void should_partial_update_project_category_icon_slug() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        final var newIconSlug = faker.rickAndMorty().location();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));

        // When
        projectCategoryService.updateCategory(existing.id(), null, null, newIconSlug, null);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(existing.name());
        assertThat(projectCategory.description()).isEqualTo(existing.description());
        assertThat(projectCategory.iconSlug()).isEqualTo(newIconSlug);
    }

    @Test
    void should_link_project_category_to_suggestion() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        existing.projects().add(UUID.randomUUID());
        final var suggestionId = ProjectCategorySuggestion.Id.random();
        final var projectId = UUID.randomUUID();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));
        when(projectCategoryStoragePort.get(suggestionId)).thenReturn(Optional.of(ProjectCategorySuggestion.of(faker.rickAndMorty().character(), projectId)));

        // When
        projectCategoryService.updateCategory(existing.id(), null, null, null, suggestionId);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(existing.name());
        assertThat(projectCategory.description()).isEqualTo(existing.description());
        assertThat(projectCategory.iconSlug()).isEqualTo(existing.iconSlug());
        assertThat(projectCategory.projects()).hasSize(2);
        assertThat(projectCategory.projects()).contains(projectId);

        verify(projectCategoryStoragePort).delete(suggestionId);
    }

    @Test
    void should_update_project_category_from_a_suggestion() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        existing.projects().add(UUID.randomUUID());
        final var newName = faker.rickAndMorty().character();
        final var newDescription = faker.rickAndMorty().quote();
        final var newIconSlug = faker.rickAndMorty().location();
        final var suggestionId = ProjectCategorySuggestion.Id.random();
        final var projectId = UUID.randomUUID();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));
        when(projectCategoryStoragePort.get(suggestionId)).thenReturn(Optional.of(ProjectCategorySuggestion.of(faker.rickAndMorty().character(), projectId)));

        // When
        projectCategoryService.updateCategory(existing.id(), newName, newDescription, newIconSlug, suggestionId);

        // Then
        final var projectCategoryCaptor = ArgumentCaptor.forClass(ProjectCategory.class);
        verify(projectCategoryStoragePort).save(projectCategoryCaptor.capture());
        final var projectCategory = projectCategoryCaptor.getValue();

        assertThat(projectCategory.id()).isEqualTo(existing.id());
        assertThat(projectCategory.name()).isEqualTo(newName);
        assertThat(projectCategory.description()).isEqualTo(newDescription);
        assertThat(projectCategory.iconSlug()).isEqualTo(newIconSlug);
        assertThat(projectCategory.projects()).hasSize(2);
        assertThat(projectCategory.projects()).contains(projectId);

        verify(projectCategoryStoragePort).delete(suggestionId);
    }

    @Test
    void should_fail_to_update_category_if_suggestion_not_found() {
        // Given
        final var existing = ProjectCategory.of(faker.rickAndMorty().character(), faker.rickAndMorty().quote(), faker.rickAndMorty().location());
        final var newName = faker.rickAndMorty().character();
        final var newDescription = faker.rickAndMorty().quote();
        final var newIconSlug = faker.rickAndMorty().location();
        final var suggestionId = ProjectCategorySuggestion.Id.random();

        when(projectCategoryStoragePort.get(existing.id())).thenReturn(Optional.of(existing));
        when(projectCategoryStoragePort.get(suggestionId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> projectCategoryService.updateCategory(existing.id(), newName, newDescription, newIconSlug, suggestionId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Project category suggestion %s not found".formatted(suggestionId));

        verify(projectCategoryStoragePort, never()).delete(suggestionId);
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