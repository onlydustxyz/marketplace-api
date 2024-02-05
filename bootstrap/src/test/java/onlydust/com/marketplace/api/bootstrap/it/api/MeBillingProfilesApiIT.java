package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

public class MeBillingProfilesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_get_individual_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(ME_GET_INDIVIDUAL_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");
    }

    @Test
    void should_get_company_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(ME_GET_COMPANY_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");
    }

    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;

    // To delete when we'll have a test with Sumsub mocked to create a billing profile with the KYC flow
    @Test
    void should_get_individual_billing_profile_given_one() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();
        individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .address(faker.address().fullAddress())
                .country(faker.address().country())
                .birthdate(faker.date().birthday())
                .firstName(faker.rickAndMorty().character())
                .lastName(faker.harryPotter().character())
                .idDocumentNumber(faker.hacker().abbreviation())
                .usCitizen(true)
                .validUntil(faker.date().past(10, TimeUnit.DAYS))
                .verificationStatus(VerificationStatusEntity.REJECTED)
                .build());

        // When
        client.get()
                .uri(ME_GET_INDIVIDUAL_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("REJECTED");
    }

    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;

    // To delete when we'll have a test with Sumsub mocked to create a billing profile with the KYB flow
    @Test
    void should_get_company_billing_profile_given_one() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .address(faker.address().fullAddress())
                .country(faker.address().country())
                .name(faker.rickAndMorty().character())
                .registrationNumber(faker.harryPotter().character())
                .euVATNumber(faker.hacker().abbreviation())
                .usEntity(true)
                .registrationDate(faker.date().past(10, TimeUnit.DAYS))
                .verificationStatus(VerificationStatusEntity.UNDER_REVIEW)
                .build());

        // When
        client.get()
                .uri(ME_GET_COMPANY_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW");
    }


    @Test
    void should_update_user_billing_profile_type() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfileType").isEqualTo("INDIVIDUAL");

        // When
        client.patch()
                .uri(getApiURI(ME_PATCH_BILLING_PROFILE_TYPE))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfileType").isEqualTo("COMPANY");
    }
}
