package onlydust.com.marketplace.api.it.bo;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import org.junit.jupiter.api.*;

import java.util.Map;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeBannerApiIT extends AbstractMarketplaceBackOfficeApiIT {

    UserAuthHelper.AuthenticatedBackofficeUser emilie;

    @BeforeEach
    void login() {
        emilie = userAuthHelper.authenticateEmilie();
    }

    @Test
    @Order(1)
    void should_raise_missing_authentication_given_no_access_token() {
        // When
        client.get()
                .uri(getApiURI(BANNERS))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();
    }

    @Test
    @Order(1)
    void should_raise_missing_authentication_given_no_permissions() {
        // Given
        final var user = userAuthHelper.authenticateAnthony();

        // When
        client.get()
                .uri(getApiURI(BANNERS))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();
    }

    @Test
    @Order(1)
    void should_get_no_banners() {
        // When
        client.get()
                .uri(getApiURI(BANNERS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {"totalPageNumber":0,"totalItemNumber":0,"hasMore":false,"nextPageIndex":0,"banners":[]}
                        """);
    }
}
