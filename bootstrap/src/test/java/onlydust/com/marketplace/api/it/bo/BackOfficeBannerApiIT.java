package onlydust.com.marketplace.api.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.backoffice.api.contract.model.BannerCreateRequest;
import onlydust.com.backoffice.api.contract.model.BannerCreateResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeBannerApiIT extends AbstractMarketplaceBackOfficeApiIT {
    private UserAuthHelper.AuthenticatedBackofficeUser emilie;
    private final Faker faker = new Faker();

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
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.post()
                .uri(getApiURI(BANNERS))
                .bodyValue(new BannerCreateRequest()
                        .text(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
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
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.post()
                .uri(getApiURI(BANNERS))
                .header("Authorization", "Bearer " + user.jwt())
                .bodyValue(new BannerCreateRequest()
                        .text(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
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

    @Test
    @Order(2)
    void should_create_banner() {
        // Given
        final var text = faker.lorem().sentence();
        final var buttonText = faker.lorem().word();
        final var buttonIconSlug = faker.internet().slug();
        final var buttonLinkUrl = URI.create(faker.internet().url());

        // When
        final var bannerId = client.post()
                .uri(getApiURI(BANNERS))
                .header("Authorization", "Bearer " + emilie.jwt())
                .bodyValue(new BannerCreateRequest()
                        .text(text)
                        .buttonText(buttonText)
                        .buttonIconSlug(buttonIconSlug)
                        .buttonLinkUrl(buttonLinkUrl)
                )
                .exchange()
                // Then
                .expectStatus()
                .isCreated()
                .expectBody(BannerCreateResponse.class)
                .returnResult().getResponseBody().getId();

        assertThat(bannerId).isNotNull();
    }

    @Test
    @Order(2)
    void should_create_banner_with_minimal_info() {
        // Given
        final var text = faker.lorem().sentence();

        // When
        final var bannerId = client.post()
                .uri(getApiURI(BANNERS))
                .header("Authorization", "Bearer " + emilie.jwt())
                .bodyValue(new BannerCreateRequest()
                        .text(text)
                )
                .exchange()
                // Then
                .expectStatus()
                .isCreated()
                .expectBody(BannerCreateResponse.class)
                .returnResult().getResponseBody().getId();

        assertThat(bannerId).isNotNull();
    }
}