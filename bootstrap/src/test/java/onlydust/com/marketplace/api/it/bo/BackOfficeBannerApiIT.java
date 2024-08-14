package onlydust.com.marketplace.api.it.bo;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.BannerCreateRequest;
import onlydust.com.backoffice.api.contract.model.BannerCreateResponse;
import onlydust.com.backoffice.api.contract.model.BannerUpdateRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BannerClosedByEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BannerRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeBannerApiIT extends AbstractMarketplaceBackOfficeApiIT {
    private UserAuthHelper.AuthenticatedBackofficeUser emilie;
    private final Faker faker = new Faker();

    @Autowired
    BannerRepository bannerRepository;

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
                        .shortDescription(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.put()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .bodyValue(new BannerUpdateRequest()
                        .shortDescription(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.delete()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.post()
                .uri(getApiURI(BANNER_VISIBLE.formatted(UUID.randomUUID())))
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.delete()
                .uri(getApiURI(BANNER_VISIBLE.formatted(UUID.randomUUID())))
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    @Order(1)
    void should_raise_missing_authentication_given_no_permissions() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

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
                        .shortDescription(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.put()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + user.jwt())
                .bodyValue(new BannerUpdateRequest()
                        .shortDescription(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.delete()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.post()
                .uri(getApiURI(BANNER_VISIBLE.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();

        // When
        client.delete()
                .uri(getApiURI(BANNER_VISIBLE.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    @Order(1)
    void should_raise_not_found() {
        // When
        client.get()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();

        // When
        client.put()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .bodyValue(new BannerUpdateRequest()
                        .shortDescription(faker.lorem().sentence())
                        .longDescription(faker.lorem().sentence())
                        .title(faker.lorem().sentence())
                        .subTitle(faker.lorem().sentence())
                )
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();

        // When
        client.delete()
                .uri(getApiURI(BANNER.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();

        // When
        client.post()
                .uri(getApiURI(BANNER_VISIBLE.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();

        // When
        client.delete()
                .uri(getApiURI(BANNER_VISIBLE.formatted(UUID.randomUUID())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();
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
    void should_get_banners() {
        // Given
        final var texts = new String[]{faker.lorem().sentence(), faker.lorem().sentence(), faker.lorem().sentence(), faker.lorem().sentence()};

        final var banners = new UUID[]{
                createBanner(texts[0]),
                createBanner(texts[1]),
                createBanner(texts[2]),
                createBanner(texts[3])
        };

        // When
        client.get()
                .uri(getApiURI(BANNERS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.totalPageNumber").isEqualTo(1)
                .jsonPath("$.totalItemNumber").isEqualTo(4)
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
                .jsonPath("$.banners.length()").isEqualTo(4)
                .jsonPath("$.banners[0].id").isEqualTo(banners[3].toString())
                .jsonPath("$.banners[0].shortDescription").isEqualTo(texts[3])
                .jsonPath("$.banners[0].visible").isEqualTo(false)
                .jsonPath("$.banners[1].id").isEqualTo(banners[2].toString())
                .jsonPath("$.banners[1].shortDescription").isEqualTo(texts[2])
                .jsonPath("$.banners[1].visible").isEqualTo(false)
                .jsonPath("$.banners[2].id").isEqualTo(banners[1].toString())
                .jsonPath("$.banners[2].shortDescription").isEqualTo(texts[1])
                .jsonPath("$.banners[2].visible").isEqualTo(false)
                .jsonPath("$.banners[3].id").isEqualTo(banners[0].toString())
                .jsonPath("$.banners[3].shortDescription").isEqualTo(texts[0])
                .jsonPath("$.banners[3].visible").isEqualTo(false)
        ;
    }

    @Test
    @Order(3)
    void should_crud_banner() {
        // Given
        final var shortDescription = faker.lorem().sentence();
        final var longDescription = faker.lorem().sentence();
        final var title = faker.lorem().word();
        final var subTitle = faker.lorem().word();
        final var buttonText = faker.lorem().word();
        final var buttonIconSlug = faker.internet().slug();
        final var buttonLinkUrl = URI.create(faker.internet().url());
        final var date = ZonedDateTime.now();

        // When
        final var bannerId = client.post()
                .uri(getApiURI(BANNERS))
                .header("Authorization", "Bearer " + emilie.jwt())
                .bodyValue(new BannerCreateRequest()
                        .shortDescription(shortDescription)
                        .longDescription(longDescription)
                        .title(title)
                        .subTitle(subTitle)
                        .buttonText(buttonText)
                        .buttonIconSlug(buttonIconSlug)
                        .buttonLinkUrl(buttonLinkUrl)
                        .date(date)
                )
                .exchange()
                // Then
                .expectStatus()
                .isCreated()
                .expectBody(BannerCreateResponse.class)
                .returnResult().getResponseBody().getId();

        assertThat(bannerId).isNotNull();

        // Add some closedBy rows
        final var banner = bannerRepository.findById(bannerId).orElseThrow();
        banner.closedBy().addAll(Set.of(
                new BannerClosedByEntity(bannerId, userAuthHelper.authenticateAntho().user().getId()),
                new BannerClosedByEntity(bannerId, userAuthHelper.authenticatePierre().user().getId()),
                new BannerClosedByEntity(bannerId, userAuthHelper.authenticateOlivier().user().getId()),
                new BannerClosedByEntity(bannerId, userAuthHelper.authenticateHayden().user().getId())
        ));
        bannerRepository.save(banner);

        client.get()
                .uri(getApiURI(BANNER.formatted(bannerId)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(bannerId.toString())
                .jsonPath("$.shortDescription").isEqualTo(shortDescription)
                .jsonPath("$.longDescription").isEqualTo(longDescription)
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.subTitle").isEqualTo(subTitle)
                .jsonPath("date").isNotEmpty()
                .jsonPath("$.visible").isEqualTo(false)
                .jsonPath("$.buttonIconSlug").isEqualTo(buttonIconSlug)
                .jsonPath("$.buttonText").isEqualTo(buttonText)
                .jsonPath("$.buttonLinkUrl").isEqualTo(buttonLinkUrl.toString())
        ;

        final var newShortDescription = faker.lorem().sentence();
        final var newLongDescription = faker.lorem().sentence();
        final var newTitle = faker.lorem().word();
        final var newSubTitle = faker.lorem().word();
        final var newButtonText = faker.lorem().word();
        final var newButtonIconSlug = faker.internet().slug();
        final var newButtonLinkUrl = URI.create(faker.internet().url());

        client.put()
                .uri(getApiURI(BANNER.formatted(bannerId)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .bodyValue(new BannerUpdateRequest()
                        .shortDescription(newShortDescription)
                        .longDescription(newLongDescription)
                        .title(newTitle)
                        .subTitle(newSubTitle)
                        .buttonText(newButtonText)
                        .buttonIconSlug(newButtonIconSlug)
                        .buttonLinkUrl(newButtonLinkUrl)
                )
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(BANNER.formatted(bannerId)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(bannerId.toString())
                .jsonPath("$.shortDescription").isEqualTo(newShortDescription)
                .jsonPath("$.longDescription").isEqualTo(newLongDescription)
                .jsonPath("$.title").isEqualTo(newTitle)
                .jsonPath("$.subTitle").isEqualTo(newSubTitle)
                .jsonPath("$.visible").isEqualTo(false)
                .jsonPath("$.buttonIconSlug").isEqualTo(newButtonIconSlug)
                .jsonPath("$.buttonText").isEqualTo(newButtonText)
                .jsonPath("$.buttonLinkUrl").isEqualTo(newButtonLinkUrl.toString())
        ;


        client.delete()
                .uri(getApiURI(BANNER.formatted(bannerId)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(BANNER.formatted(bannerId)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();
    }

    @Test
    @Order(3)
    void should_create_banner_with_minimal_info() {
        // Given
        final var text = faker.lorem().sentence();

        // When
        final var bannerId = createBanner(text);

        assertThat(bannerId).isNotNull();

        client.get()
                .uri(getApiURI(BANNER.formatted(bannerId)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(bannerId.toString())
                .jsonPath("$.shortDescription").isEqualTo(text)
                .jsonPath("$.visible").isEqualTo(false)
                .jsonPath("$.buttonIconSlug").doesNotExist()
                .jsonPath("$.buttonText").doesNotExist()
                .jsonPath("$.buttonLinkUrl").doesNotExist()
        ;
    }

    @Test
    @Order(4)
    void should_hide_and_show_banners() {
        // Given
        final var banner1Id = createBanner(faker.lorem().sentence());
        final var banner2Id = createBanner(faker.lorem().sentence());
        final var banner3Id = createBanner(faker.lorem().sentence());
        final var banner4Id = createBanner(faker.lorem().sentence());

        // When
        // Show banner 1
        client.post()
                .uri(getApiURI(BANNER_VISIBLE.formatted(banner1Id)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        // Then
        getBannersPage()
                .jsonPath("$.banners[?(@.id == '%s')].visible".formatted(banner1Id)).isEqualTo(true)
                .jsonPath("$.banners[?(@.id != '%s' && @.visible == true)]".formatted(banner1Id)).doesNotExist();

        // When
        // Show banner 3
        client.post()
                .uri(getApiURI(BANNER_VISIBLE.formatted(banner3Id)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        // Then
        getBannersPage()
                .jsonPath("$.banners[?(@.id == '%s')].visible".formatted(banner3Id)).isEqualTo(true)
                .jsonPath("$.banners[?(@.id != '%s' && @.visible == true)]".formatted(banner3Id)).doesNotExist();


        // When
        // Hide banner 3
        client.delete()
                .uri(getApiURI(BANNER_VISIBLE.formatted(banner3Id)))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        // Then
        getBannersPage()
                .jsonPath("$.banners[?(@.visible == false)]").isNotEmpty()
                .jsonPath("$.banners[?(@.visible == true)]").doesNotExist()
        ;
    }

    private WebTestClient.@NonNull BodyContentSpec getBannersPage() {
        return client.get()
                .uri(getApiURI(BANNERS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody();
    }

    private UUID createBanner(String shortDescription) {
        return client.post()
                .uri(getApiURI(BANNERS))
                .header("Authorization", "Bearer " + emilie.jwt())
                .bodyValue(new BannerCreateRequest()
                        .shortDescription(shortDescription)
                        .longDescription(shortDescription + shortDescription)
                        .title(shortDescription + shortDescription + shortDescription)
                        .subTitle(shortDescription + shortDescription + shortDescription + shortDescription)
                )
                .exchange()
                // Then
                .expectStatus()
                .isCreated()
                .expectBody(BannerCreateResponse.class)
                .returnResult().getResponseBody().getId();
    }
}
