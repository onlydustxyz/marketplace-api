package onlydust.com.marketplace.api.it.bo;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.BillingProfileType;
import onlydust.com.backoffice.api.contract.model.OnlyDustError;
import onlydust.com.backoffice.api.contract.model.UserSearchPage;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    @CsvSource({
            "login==ofux,true",
            "email==*gmail.com,true",
            "githubUserId==595505,true",
            "billingProfile.name==pixelfact,false",
            "billingProfile.kyc.lastName==Oucif,false",
            "billingProfile.kyb.name==pixelfact,false",
            "language1.name==Rust,false",
            "ecosystem1.name==Starknet,false",
            "language2.name==Java,false",
            "ecosystem2.name==Zama,false"
    })
    void should_search_users_by_using_any_field_as_criteria(String query, boolean sortable) {
        searchUsers(query);
        if (sortable)
            searchUsers(query, query.substring(0, query.indexOf("==")) + ",asc");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "global",
            "language1",
            "language2",
            "language3",
            "ecosystem1",
            "ecosystem2",
            "ecosystem3"
    })
    void should_search_users_by_using_any_stats_field_as_criteria(String field) {
        List.of("contributionCount", "rewardCount", "pendingRewardCount", "totalUsdAmount", "maxUsdAmount", "rank").forEach(stat -> {
            List.of("==", ">", "<", ">=", "<=").forEach(op -> {
                searchUsers(field + "." + stat + op + "7");
            });
        });
    }

    @Test
    void should_search_users_with_criteria() {
        final var rustAndCairoCount = searchUsers("((language1.name==Rust);(language2.name==Cairo))").getTotalItemNumber();
        assertThat(rustAndCairoCount).isEqualTo(16);
        final var rustOrCairoCount = searchUsers("((language1.name==Rust),(language2.name==Cairo))").getTotalItemNumber();
        assertThat(rustOrCairoCount).isEqualTo(214);

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

        users = searchUsers("language1.name==Rust and language1.totalUsdAmount>1000").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            final var rust = user.getLanguage().stream().filter(l -> l.getName().equals("Rust")).toList();
            assertThat(rust).hasSize(1);
            rust.forEach(r -> assertThat(r.getTotalUsdAmount()).isGreaterThan(BigDecimal.valueOf(1000)));
        });

        users = searchUsers("language1.name==Rust and language1.totalUsdAmount>1000 and language2.name==Cairo and language2.rank<100").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            final var rust = user.getLanguage().stream().filter(l -> l.getName().equals("Rust")).toList();
            assertThat(rust).hasSize(1);
            rust.forEach(r -> assertThat(r.getTotalUsdAmount()).isGreaterThan(BigDecimal.valueOf(1000)));
            final var cairo = user.getLanguage().stream().filter(l -> l.getName().equals("Cairo")).toList();
            assertThat(cairo).hasSize(1);
            cairo.forEach(r -> assertThat(r.getRank()).isLessThan(100));
        });

        users = searchUsers("ecosystem1.name==Starknet and ecosystem1.contributionCount>50").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            final var starknet = user.getEcosystem().stream().filter(l -> l.getName().equals("Starknet")).toList();
            assertThat(starknet).hasSize(1);
            starknet.forEach(r -> assertThat(r.getContributionCount()).isGreaterThan(50));
        });

        users = searchUsers("ecosystem1.name==Starknet and ecosystem1.contributionCount>50 and ecosystem5.name=ilike=aztec and ecosystem5.rank<200").getUsers();
        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            final var starknet = user.getEcosystem().stream().filter(l -> l.getName().equals("Starknet")).toList();
            assertThat(starknet).hasSize(1);
            starknet.forEach(r -> assertThat(r.getContributionCount()).isGreaterThan(50));
            final var aztec = user.getEcosystem().stream().filter(l -> l.getName().equals("Aztec")).toList();
            assertThat(aztec).hasSize(1);
            aztec.forEach(r -> assertThat(r.getRank()).isLessThan(200));
        });

        users = searchUsers("billingProfile.verificationStatus==NOT_STARTED and billingProfile.type==SELF_EMPLOYED").getUsers();
        assertThat(users).isNotEmpty();
        assertThat(users).allMatch(user -> user.getBillingProfile().stream().anyMatch(bp -> bp.getStatus() == VerificationStatus.NOT_STARTED && bp.getType() == BillingProfileType.SELF_EMPLOYED));

        users = searchUsers("billingProfile.kyb.country==FRA").getUsers();
        assertThat(users).isNotEmpty();
        assertThat(users).allMatch(user -> user.getBillingProfile().stream().anyMatch(bp -> bp.getKyb() != null && bp.getKyb().getCountryCode().equals("FRA")));
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
    }

    @Test
    void should_return_400_when_query_is_invalid() {
        var error = searchUsersWithWrongQuery("foo.bar<200", null);
        assertThat(error.getMessage()).isEqualTo("Invalid query: 'foo.bar<200' and/or sort: 'login' (UnknownProperty error)");

        error = searchUsersWithWrongQuery("global.rank>foo", null);
        assertThat(error.getMessage()).isEqualTo("Invalid query: 'global.rank>foo' and/or sort: 'login' (Conversion error)");

        error = searchUsersWithWrongQuery("login==ofux", "foo");
        assertThat(error.getMessage()).isEqualTo("Invalid query: 'login==ofux' and/or sort: 'foo' (UnknownProperty error)");

        error = searchUsersWithWrongQuery("login==ofux", "language1.rank");
        assertThat(error.getMessage()).isEqualTo("Invalid sort: 'language1.rank' (global, language and ecosystem are not allowed)");

        error = searchUsersWithWrongQuery("login==ofux", "ecosystem3.rank");
        assertThat(error.getMessage()).isEqualTo("Invalid sort: 'ecosystem3.rank' (global, language and ecosystem are not allowed)");

        error = searchUsersWithWrongQuery("login==ofux", "global.contributionCount");
        assertThat(error.getMessage()).isEqualTo("Invalid sort: 'global.contributionCount' (global, language and ecosystem are not allowed)");
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
