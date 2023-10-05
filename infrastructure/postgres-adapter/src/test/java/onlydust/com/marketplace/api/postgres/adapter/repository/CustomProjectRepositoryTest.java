package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository.FIND_PROJECTS_BASE_QUERY;
import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository.buildQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomProjectRepositoryTest {

    private final Faker faker = new Faker();

    @Test
    void should_build_query_given_empty_sort_search_order() {
        // Given

        // When
        final String query = buildQuery(List.of(), List.of(), null, null, null);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY, query);
    }

    @Test
    void should_build_query_given_search() {
        // Given
        final String search = faker.pokemon().name();

        // When
        final String query = buildQuery(List.of(), List.of(), null, search, null);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY
                        + " where (search_project.short_description like '%" + search + "%' or search_project.name " +
                        "like '%" + search + "%')"
                , query);
    }

    @Test
    void should_build_query_given_a_contributors_count_sort() {
        // Given
        final ProjectCardView.SortBy sort = ProjectCardView.SortBy.CONTRIBUTORS_COUNT;

        // When
        final String query = buildQuery(List.of(), List.of(), null, null, sort);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY
                        + " order by search_project.contributors_count desc"
                , query);
    }

    @Test
    void should_build_query_given_a_repo_count_sort() {
        // Given
        final ProjectCardView.SortBy sort = ProjectCardView.SortBy.REPOS_COUNT;

        // When
        final String query = buildQuery(List.of(), List.of(), null, null, sort);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY
                        + " order by search_project.repo_count desc"
                , query);
    }

    @Test
    void should_build_query_given_a_name_sort() {
        // Given
        final ProjectCardView.SortBy sort = ProjectCardView.SortBy.NAME;

        // When
        final String query = buildQuery(List.of(), List.of(), null, null, sort);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY
                        + " order by search_project.name asc"
                , query);
    }

    @Test
    void should_build_query_given_a_rank_sort() {
        // Given
        final ProjectCardView.SortBy sort = ProjectCardView.SortBy.RANK;

        // When
        final String query = buildQuery(List.of(), List.of(), null, null, sort);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY
                        + " order by search_project.rank desc"
                , query);
    }

    @Test
    void should_build_query_given_mine() {
        // Given
        final UUID userId = UUID.randomUUID();

        // When
        final String query = buildQuery(List.of(), List.of(), userId, null, null);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY
                        + " where (search_project.pl_user_id = '" + userId + "')"
                , query);
    }

    @Test
    void should_build_query_given_a_list_of_sponsors() {
        // Given
        final List<String> sponsors = List.of(faker.pokemon().name(), faker.pokemon().location());

        // When
        final String query1 = buildQuery(List.of(), sponsors.subList(0, 1), null, null, null);
        final String query2 = buildQuery(List.of(), sponsors, null, null, null);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY + " where (search_project.sponsor_name in ('" + sponsors.get(0) + "'))",
                query1);
        assertEquals(FIND_PROJECTS_BASE_QUERY + " where (search_project.sponsor_name in ('" + sponsors.get(0) + "','" + sponsors.get(1) + "'))", query2);
    }

    @Test
    void should_build_query_given_a_list_of_technologies() {
        // Given
        final List<String> technologies = List.of(faker.pokemon().name(), faker.pokemon().location());

        // When
        final String query1 = buildQuery(technologies.subList(0, 1), List.of(), null, null, null);
        final String query2 = buildQuery(technologies, List.of(), null, null, null);

        // Then
        assertEquals(FIND_PROJECTS_BASE_QUERY + " where (search_project.languages like '%\"" + technologies.get(0) +
                        "\"%')",
                query1);
        assertEquals(FIND_PROJECTS_BASE_QUERY + " where (search_project.languages like '%\""
                + technologies.get(0) + "\"%' or" +
                " search_project.languages like '%\"" + technologies.get(1) + "\"%')", query2);
    }
}
