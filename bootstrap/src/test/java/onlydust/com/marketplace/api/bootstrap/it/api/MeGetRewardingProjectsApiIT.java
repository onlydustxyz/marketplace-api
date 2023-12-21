package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetRewardingProjectsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_get_all_my_rewarding_projects() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDING_PROJECTS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.projects[0].slug").isEqualTo("aldbaran-du-taureau")
                .json("""
                        {
                          "projects": [
                            {
                              "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                              "slug": "aldbaran-du-taureau",
                              "name": "Aldébaran du Taureau",
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed",
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "5aabf0f1-7495-4bff-8de2-4396837ce6b4",
                              "slug": "marketplace-2",
                              "name": "Marketplace 2",
                              "shortDescription": "awesome marketplace",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                              "slug": "pizzeria-yoshi-",
                              "name": "Pizzeria Yoshi !",
                              "shortDescription": "Miaaaam une pizza !",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                              "slug": "kaaper",
                              "name": "kaaper",
                              "shortDescription": "Documentation generator for Cairo projects.",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "29cdf359-f60c-41a0-8b11-18d6841311f6",
                              "slug": "kaaper-3",
                              "name": "kaaper 3",
                              "shortDescription": "Yet another kaaperrr",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "d4e8ab3b-a4a8-493d-83bd-a4c8283b94f9",
                              "slug": "oscars-awesome-project",
                              "name": "oscar's awesome project",
                              "shortDescription": "awesome project",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            }
                          ]
                        }
                        """);
    }
}
