package onlydust.com.marketplace.api.it.bo;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.OnlyDustError;
import onlydust.com.backoffice.api.contract.model.UserSearchPage;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TagBO
public class BackofficeUserSearchApiIT extends AbstractMarketplaceBackOfficeApiIT {
    UserAuthHelper.AuthenticatedBackofficeUser mehdi;

    @BeforeEach
    void setUp() {
        mehdi = userAuthHelper.authenticateBackofficeUser("pixelfact.company@gmail.com", List.of(BackofficeUser.Role.BO_READER));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "login==ofux",
            "email==*gmail.com",
            "githubUserId==595505",
            "language.name==Rust",
            "ecosystem.name==Starknet"
    })
    void should_search_users_by_using_any_field_as_criteria(String query) {
        searchUsers(query);
        searchUsers(query, query.substring(0, query.indexOf("==")) + ",asc");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "global",
            "language",
            "ecosystem"
    })
    void should_search_users_by_using_any_stats_field_as_criteria(String field) {
        List.of("contributionCount", "rewardCount", "pendingRewardCount", "totalUsdAmount", "maxUsdAmount", "rank").forEach(stat -> {
            List.of("==", ">", "<", ">=", "<=").forEach(op -> {
                searchUsers(field + "." + stat + op + "7");
                searchUsers(field + "." + stat + op + "7", field + "." + stat + ",desc");
            });
        });
    }

    @Test
    void should_search_users_with_criteria() {
        var users = searchUsers("login==ofux").getUsers();
        assertThat(users).hasSize(1);
        users.forEach(user -> {
            assertThat(user.getLogin()).isEqualTo("ofux");
        });

        users = searchUsers("global.rank<100").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            assertThat(user.getGlobal().getRank()).isLessThan(100);
        });

        users = searchUsers("language.name==Rust and language.totalUsdAmount>1000").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            final var rust = user.getLanguage().stream().filter(l -> l.getName().equals("Rust")).toList();
            assertThat(rust).hasSize(1);
            rust.forEach(r -> assertThat(r.getTotalUsdAmount()).isGreaterThan(BigDecimal.valueOf(1000)));
        });

        users = searchUsers("ecosystem.name==Starknet and ecosystem.contributionCount>50").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            final var starknet = user.getEcosystem().stream().filter(l -> l.getName().equals("Starknet")).toList();
            assertThat(starknet).hasSize(1);
            starknet.forEach(r -> assertThat(r.getContributionCount()).isGreaterThan(50));
        });
    }

    @Test
    void should_search_users_with_sorting() {
        var users = searchUsers("global.rank<200", "login,desc", 10).getUsers();
        assertThat(users).hasSize(10);
        assertThat(users).isSortedAccordingTo((u1, u2) -> u2.getLogin().compareTo(u1.getLogin()));

        var users2 = searchUsers("global.rank<200", "login,desc", 10, 2).getUsers();
        users.addAll(users2);
        assertThat(users).hasSize(20);
        assertThat(users).isSortedAccordingTo((u1, u2) -> u2.getLogin().compareTo(u1.getLogin()));

        users = searchUsers("global.rank<200", "global.rank", 10).getUsers();
        assertThat(users).hasSize(10);
        assertThat(users).isSortedAccordingTo((u1, u2) -> u1.getGlobal().getRank().compareTo(u2.getGlobal().getRank()));

        users = searchUsers("language.name==Rust", "language.totalUsdAmount,desc", 10).getUsers();
        assertThat(users).hasSize(10);
        assertThat(users).isSortedAccordingTo((u1, u2) -> u2.getLanguage().stream().filter(l -> l.getName().equals("Rust")).findFirst().orElseThrow().getTotalUsdAmount()
                .compareTo(u1.getLanguage().stream().filter(l -> l.getName().equals("Rust")).findFirst().orElseThrow().getTotalUsdAmount()));

        users = searchUsers("global.rank<200", "global.rank;global.contributionCount,desc", 200).getUsers();
        assertThat(users).hasSize(200);
        assertThat(users).isSortedAccordingTo((u1, u2) -> {
            final var rankCompare = u1.getGlobal().getRank().compareTo(u2.getGlobal().getRank());
            if (rankCompare != 0) {
                return rankCompare;
            }
            return u2.getGlobal().getContributionCount().compareTo(u1.getGlobal().getContributionCount());
        });
    }

    @Test
    void should_return_400_when_query_is_invalid() {
        var error = searchUsersWithWrongQuery("foo.bar<200", null);
        assertThat(error.getMessage()).isEqualTo("Invalid query: 'foo.bar<200' and/or sort: 'login' (UnknownProperty error)");

        error = searchUsersWithWrongQuery("global.rank>foo", null);
        assertThat(error.getMessage()).isEqualTo("Invalid query: 'global.rank>foo' and/or sort: 'login' (Conversion error)");

        error = searchUsersWithWrongQuery("login==ofux", "foo");
        assertThat(error.getMessage()).isEqualTo("Invalid query: 'login==ofux' and/or sort: 'foo' (UnknownProperty error)");
    }

    @Test
    void should_search_users_and_export_to_csv() {
        var csv = searchUsersCSV("global.rank<=8", null, 1000, 0);
        assertThat(csv).isEqualToIgnoringWhitespace("""
                Login,Email,Telegram
                AnthonyBuisset,abuisset@gmail.com,https://t.me/abuisset
                Bernardstanislas,bernardstanislas@gmail.com,
                ClementWalter,,
                PierreOucif,pierre.oucif@gadz.org,
                danilowhk,,
                gregcha,gcm.charles@gmail.com,https://t.me/gregoirecharles
                ofux,olivier.fuxet@gmail.com,
                oscarwroche,oscar.w.roche@gmail.com,
                """);
    }

    private UserSearchPage searchUsers(@NonNull String query) {
        return searchUsers(query, null, 5, 0);
    }

    private UserSearchPage searchUsers(@NonNull String query, String sort) {
        return searchUsers(query, sort, 5, 0);
    }

    private UserSearchPage searchUsers(@NonNull String query, String sort, int pageSize) {
        return searchUsers(query, sort, pageSize, 0);
    }

    private UserSearchPage searchUsers(@NonNull String query, String sort, int pageSize, int pageIndex) {
        final var queryParams = new HashMap<>(Map.of("pageIndex", "" + pageIndex, "pageSize", "" + pageSize, "query", query));
        if (sort != null) {
            queryParams.put("sort", sort);
        }
        final var response = client.get()
                .uri(getApiURI(GET_SEARCH_USERS, queryParams))
                .header("Authorization", "Bearer " + mehdi.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(UserSearchPage.class).returnResult().getResponseBody();
        return response;
    }

    private String searchUsersCSV(@NonNull String query, String sort, int pageSize, int pageIndex) {
        final var queryParams = new HashMap<>(Map.of("pageIndex", "" + pageIndex, "pageSize", "" + pageSize, "query", query));
        if (sort != null) {
            queryParams.put("sort", sort);
        }
        final var response = client.get()
                .uri(getApiURI(GET_SEARCH_USERS_CSV, queryParams))
                .header("Authorization", "Bearer " + mehdi.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class).returnResult().getResponseBody();
        return response;
    }

    private OnlyDustError searchUsersWithWrongQuery(@NonNull String query, String sort) {
        final var queryParams = new HashMap<>(Map.of("pageIndex", "0", "pageSize", "5", "query", query));
        if (sort != null) {
            queryParams.put("sort", sort);
        }
        final var response = client.get()
                .uri(getApiURI(GET_SEARCH_USERS, queryParams))
                .header("Authorization", "Bearer " + mehdi.jwt())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(OnlyDustError.class).returnResult().getResponseBody();
        return response;
    }
}
