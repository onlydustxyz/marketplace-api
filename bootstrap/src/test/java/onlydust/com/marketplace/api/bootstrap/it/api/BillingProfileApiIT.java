package onlydust.com.marketplace.api.bootstrap.it.api;

import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

public class BillingProfileApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_be_authenticated() {
        // When
        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .bodyValue("""
                        {
                          "name": "test",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void should_create_all_type_of_billing_profiles() {
        final String jwt =
                userAuthHelper.newFakeUser(UUID.randomUUID(), faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                        faker.internet().url(), false).jwt();

        /// When
        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "company",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("company")
                .jsonPath("$.type").isEqualTo("COMPANY")
                .jsonPath("$.kyb.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty();

        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "company",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("company")
                .jsonPath("$.type").isEqualTo("COMPANY")
                .jsonPath("$.kyb.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty();


        /// When
        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "self_employed",
                          "type": "SELF_EMPLOYED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("self_employed")
                .jsonPath("$.type").isEqualTo("SELF_EMPLOYED")
                .jsonPath("$.kyb.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty();

        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "self_employed",
                          "type": "SELF_EMPLOYED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("self_employed")
                .jsonPath("$.type").isEqualTo("SELF_EMPLOYED")
                .jsonPath("$.kyb.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty();


        /// When
        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "individual",
                          "type": "INDIVIDUAL"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("individual")
                .jsonPath("$.type").isEqualTo("INDIVIDUAL")
                .jsonPath("$.kyc.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty();

        // When
        client.post()
                .uri(getApiURI(POST_BILLING_PROFILES))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "individual",
                          "type": "INDIVIDUAL"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // When
        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES_V2))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[?(@.type == 'SELF_EMPLOYED' && @.name == 'self_employed')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY' && @.name == 'company')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'INDIVIDUAL' && @.name == 'individual')]")
                .value(jsonArray -> Assertions.assertEquals(1, ((JSONArray) jsonArray).toArray().length));
    }
}
