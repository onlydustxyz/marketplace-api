package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectListRepository.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomProjectListRepositoryTest {

    protected final Faker faker = new Faker();

    @Nested
    public class ForAuthenticatedUser {
        private static final String FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT =
                FIND_PROJECTS_FOR_USER_BASE_QUERY.replace(
                        "%order_by%", "order by search_project" +
                                ".project_id");


        @Test
        void should_build_query_given_empty_sort_search_order() {
            // Given

            // When
            final String query = buildQueryForUser(List.of(), List.of(), null, null, false);
            ;
            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT, query);
        }

        @Test
        void should_build_query_given_search() {
            // Given
            final String search = faker.pokemon().name();

            // When
            final String query = buildQueryForUser(List.of(), List.of(), search, null, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT
                            + " and (search_project.short_description like '%:search%' or search_project.name " +
                            "like '%:search%')"
                    , query);
        }

        @Test
        void should_build_query_given_a_contributors_count_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.CONTRIBUTORS_COUNT;

            // When
            final String query = buildQueryForUser(List.of(), List.of(), null, sort, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY.replace("%order_by%", "order by " +
                            "search_project.contributors_count " +
                            "desc")
                    , query);
        }

        @Test
        void should_build_query_given_a_repo_count_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.REPOS_COUNT;

            // When
            final String query = buildQueryForUser(List.of(), List.of(), null, sort, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY.replace("%order_by%", "order by " +
                            "search_project.repo_count desc")
                    , query);
        }

        @Test
        void should_build_query_given_a_name_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.NAME;

            // When
            final String query = buildQueryForUser(List.of(), List.of(), null, sort, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY.replace("%order_by%", "order by " +
                            "search_project.name")
                    , query);
        }

        @Test
        void should_build_query_given_a_rank_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.RANK;

            // When
            final String query = buildQueryForUser(List.of(), List.of(), null, sort, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY.replace("%order_by%", "order by " +
                            "search_project.rank desc")
                    , query);
        }

        @Test
        void should_build_query_given_a_list_of_sponsors() {
            // Given
            final List<String> sponsors = List.of(faker.pokemon().name(), faker.pokemon().location());

            // When
            final String query1 = buildQueryForUser(List.of(), sponsors.subList(0, 1), null, null, false);
            final String query2 = buildQueryForUser(List.of(), sponsors, null, null, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.sponsor_name in " +
                            "('" + sponsors.get(0) + "'))",
                    query1);
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.sponsor_name in " +
                    "('" + sponsors.get(0) + "','" + sponsors.get(1) + "'))", query2);
        }

        @Test
        void should_build_query_given_a_list_of_technologies() {
            // Given
            final List<String> technologies = List.of(faker.pokemon().name(), faker.pokemon().location());

            // When
            final String query1 = buildQueryForUser(technologies.subList(0, 1), List.of(), null, null, false);
            final String query2 = buildQueryForUser(technologies, List.of(), null, null, false);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.languages like " +
                            "'%\"" + technologies.get(0) +
                            "\"%')",
                    query1);
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.languages like " +
                    "'%\""
                    + technologies.get(0) + "\"%' or" +
                    " search_project.languages like '%\"" + technologies.get(1) + "\"%')", query2);
        }

        @Test
        void should_build_query_given_mine() {
            // Given

            // When
            final String mine_query = buildQueryForUser(List.of(), List.of(), null, null, true);

            // Then
            assertEquals(FIND_PROJECTS_FOR_USER_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.pl_user_id = " +
                    ":userId)", mine_query);
        }
    }


    @Nested
    public class ForAnonymousUser {

        private static final String FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT = FIND_PROJECTS_BASE_QUERY.replace(
                "%order_by%", "order by search_project" +
                        ".project_id");

        @Test
        void should_build_query_given_empty_sort_search_order() {
            // Given

            // When
            final String query = buildQuery(List.of(), List.of(), null, null);
            ;
            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT, query);
        }

        @Test
        void should_build_query_given_search() {
            // Given
            final String search = faker.pokemon().name();

            // When
            final String query = buildQuery(List.of(), List.of(), search, null);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT
                            + " and (search_project.short_description like '%:search%' or search_project.name " +
                            "like '%:search%')"
                    , query);
        }

        @Test
        void should_build_query_given_a_contributors_count_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.CONTRIBUTORS_COUNT;

            // When
            final String query = buildQuery(List.of(), List.of(), null, sort);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY.replace("%order_by%", "order by search_project.contributors_count " +
                            "desc")
                    , query);
        }

        @Test
        void should_build_query_given_a_repo_count_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.REPOS_COUNT;

            // When
            final String query = buildQuery(List.of(), List.of(), null, sort);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY.replace("%order_by%", "order by search_project.repo_count desc")
                    , query);
        }

        @Test
        void should_build_query_given_a_name_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.NAME;

            // When
            final String query = buildQuery(List.of(), List.of(), null, sort);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY.replace("%order_by%", "order by search_project.name")
                    , query);
        }

        @Test
        void should_build_query_given_a_rank_sort() {
            // Given
            final ProjectCardView.SortBy sort = ProjectCardView.SortBy.RANK;

            // When
            final String query = buildQuery(List.of(), List.of(), null, sort);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY.replace("%order_by%", "order by search_project.rank desc")
                    , query);
        }

        @Test
        void should_build_query_given_a_list_of_sponsors() {
            // Given
            final List<String> sponsors = List.of(faker.pokemon().name(), faker.pokemon().location());

            // When
            final String query1 = buildQuery(List.of(), sponsors.subList(0, 1), null, null);
            final String query2 = buildQuery(List.of(), sponsors, null, null);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.sponsor_name in ('" + sponsors.get(0) + "'))",
                    query1);
            assertEquals(FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.sponsor_name in ('" + sponsors.get(0) + "','" + sponsors.get(1) + "'))", query2);
        }

        @Test
        void should_build_query_given_a_list_of_technologies() {
            // Given
            final List<String> technologies = List.of(faker.pokemon().name(), faker.pokemon().location());

            // When
            final String query1 = buildQuery(technologies.subList(0, 1), List.of(), null, null);
            final String query2 = buildQuery(technologies, List.of(), null, null);

            // Then
            assertEquals(FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.languages like '%\"" + technologies.get(0) +
                            "\"%')",
                    query1);
            assertEquals(FIND_PROJECTS_BASE_QUERY_WITH_DEFAULT_SORT + " and (search_project.languages like '%\""
                    + technologies.get(0) + "\"%' or" +
                    " search_project.languages like '%\"" + technologies.get(1) + "\"%')", query2);
        }
    }


}
