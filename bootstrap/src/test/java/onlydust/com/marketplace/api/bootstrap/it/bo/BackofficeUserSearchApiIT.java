package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.UserSearchPage;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.suites.tags.TagBO;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
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
        searchUsers(query).getUsers();
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
                searchUsers(field + "." + stat + op + "7").getUsers();
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

    private UserSearchPage searchUsers(String query) {
        final var response = client.get()
                .uri(getApiURI(GET_SEARCH_USERS, Map.of("pageIndex", "0", "pageSize", "5", "query", query)))
                .header("Authorization", "Bearer " + mehdi.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(UserSearchPage.class).returnResult().getResponseBody();
        return response;
    }
}
