package onlydust.com.marketplace.api.it.api;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BannerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;


public class BannerApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    BannerRepository bannerRepository;

    final Faker faker = new Faker();

    List<BannerEntity> banners = List.of(randomHiddenBanner(), randomHiddenBanner(), randomHiddenBanner(), randomHiddenBanner());

    @BeforeEach
    void setup() {
        bannerRepository.deleteAll();
        bannerRepository.saveAll(banners);
    }

    @Test
    void should_return_visible_banner() {
        // Given
        bannerRepository.save(banners.get(1).visible(true));

        // When
        client.get()
                .uri(getApiURI(BANNER))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(banners.get(1).id().toString())
                .jsonPath("$.text").isEqualTo(banners.get(1).text())
                .jsonPath("$.buttonText").isEqualTo(banners.get(1).buttonText())
                .jsonPath("$.buttonIconSlug").isEqualTo(banners.get(1).buttonIconSlug())
                .jsonPath("$.buttonLinkUrl").isEqualTo(banners.get(1).buttonLinkUrl())
        ;
    }

    @Test
    void should_return_nothing_if_no_visible_banner() {
        // When
        client.get()
                .uri(getApiURI(BANNER))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    private BannerEntity randomHiddenBanner() {
        return new BannerEntity(UUID.randomUUID(),
                faker.lorem().sentence(),
                faker.lorem().word(),
                faker.internet().slug(),
                faker.internet().url(),
                false,
                faker.date().birthday().toInstant().atZone(ZoneOffset.UTC));
    }
}
