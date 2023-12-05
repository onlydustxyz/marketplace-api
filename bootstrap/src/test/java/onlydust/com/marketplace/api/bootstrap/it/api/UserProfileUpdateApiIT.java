package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class UserProfileUpdateApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_update_user_profile() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // Proves that the initial user profile is different from the updated one
        client.get()
                .uri(getApiURI(ME_GET_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.location").isEqualTo("Vence, France")
                .jsonPath("$.bio").isEqualTo("FullStack engineerr")
                .jsonPath("$.avatarUrl").isEqualTo(
                        "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp")
                .jsonPath("$.website").isEqualTo("https://linktr.ee/abuisset")
                .jsonPath("$.cover").isEqualTo("BLUE")
                .jsonPath("$.technologies.length()").isEqualTo(14)
                .jsonPath("$.technologies['Rust']").isEqualTo(404344)
                .jsonPath("$.allocatedTimeToContribute").isEqualTo("NONE")
                .jsonPath("$.isLookingForAJob").isEqualTo(false)
                .jsonPath("$.contacts.length()").isEqualTo(6)
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')].visibility").isEqualTo("private")
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')].channel").isEqualTo("EMAIL")
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM");

        // When
        client.put()
                .uri(getApiURI(ME_PUT_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "avatarUrl": "https://foobar.org/plop.jpg",
                            "location": "Paris, France",
                            "bio": "FullStack engineer",
                            "website": "https://croute.org",
                            "technologies": {
                                "C++": 100,
                                "Rust": 90,
                                "Java": 20
                            },
                            "cover": "YELLOW",
                            "contacts": [
                                {
                                    "contact": "yolo@croute.com",
                                    "channel": "EMAIL",
                                    "visibility": "public"
                                },
                                {
                                    "contact": "https://t.me/yolocroute",
                                    "channel": "TELEGRAM",
                                    "visibility": "private"
                                }
                            ],
                            "allocatedTimeToContribute": "ONE_TO_THREE_DAYS",
                            "isLookingForAJob": true
                        }
                        """)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.location").isEqualTo("Paris, France")
                .jsonPath("$.bio").isEqualTo("FullStack engineer")
                .jsonPath("$.avatarUrl").isEqualTo("https://foobar.org/plop.jpg")
                .jsonPath("$.website").isEqualTo("https://croute.org")
                .jsonPath("$.cover").isEqualTo("YELLOW")
                .jsonPath("$.technologies.length()").isEqualTo(3)
                .jsonPath("$.technologies['C++']").isEqualTo(100)
                .jsonPath("$.technologies['Rust']").isEqualTo(90)
                .jsonPath("$.technologies['Java']").isEqualTo(20)
                .jsonPath("$.allocatedTimeToContribute").isEqualTo("ONE_TO_THREE_DAYS")
                .jsonPath("$.isLookingForAJob").isEqualTo(true)
                .jsonPath("$.contacts.length()").isEqualTo(2)
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='antho')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='yolo@croute.com')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='yolo@croute.com')].channel").isEqualTo("EMAIL")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/yolocroute')].visibility").isEqualTo("private")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/yolocroute')].channel").isEqualTo("TELEGRAM");
    }

    @Test
    void should_return_an_unauthorized_error() {
        // Given

        // When
        client.put()
                .uri(getApiURI(ME_PUT_PROFILE))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "location": "Paris, France",
                            "bio": "FullStack engineer",
                            "website": "https://croute.org",
                            "technologies": {
                                "C++": 100,
                                "Rust": 90,
                                "Java": 20
                            },
                            "cover": "YELLOW",
                            "contacts": [
                                {
                                    "contact": "yolo@croute.com",
                                    "channel": "EMAIL",
                                    "visibility": "public"
                                },
                                {
                                    "contact": "https://t.me/yolocroute",
                                    "channel": "TELEGRAM",
                                    "visibility": "private"
                                }
                            ],
                            "allocatedTimeToContribute": "ONE_TO_THREE_DAYS",
                            "isLookingForAJob": true
                        }
                        """)
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }
}
