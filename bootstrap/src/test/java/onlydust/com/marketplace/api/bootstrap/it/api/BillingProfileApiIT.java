package onlydust.com.marketplace.api.bootstrap.it.api;

import net.minidev.json.JSONArray;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BillingProfileApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    PayoutPreferenceFacadePort payoutPreferenceFacadePort;

    @Test
    void should_be_authenticated() {
        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.kyb.id").isNotEmpty();

        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(true)
                .jsonPath("$.id").isNotEmpty();


        /// When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.kyb.id").isNotEmpty();

        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.kyb.id").isNotEmpty();


        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.kyc.id").isNotEmpty();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
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
                .uri(getApiURI(ME_BILLING_PROFILES))
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

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[?(@.type == 'SELF_EMPLOYED')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'INDIVIDUAL')]")
                .value(jsonArray -> Assertions.assertEquals(1, ((JSONArray) jsonArray).toArray().length));
    }

    @Test
    void should_get_update_delete_billing_profile_by_id() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                faker.internet().url(), false);
        final String jwt =
                authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());
        final CompanyBillingProfile companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);
        final SelfEmployedBillingProfile selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId,
                faker.rickAndMorty().character(), null);
        final IndividualBillingProfile individualBillingProfile = billingProfileService.createIndividualBillingProfile(userId,
                faker.rickAndMorty().character(), null);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(companyBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(companyBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        client.put()
                .uri(BILLING_PROFILES_TYPE_BY_ID.formatted(companyBillingProfile.id().value().toString()))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                        "type": "SELF_EMPLOYED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo("SELF_EMPLOYED")
                .jsonPath("$.status").isEqualTo(companyBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        client.put()
                .uri(BILLING_PROFILES_TYPE_BY_ID.formatted(companyBillingProfile.id().value().toString()))
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
                .isNoContent();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(companyBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(companyBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(selfEmployedBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(selfEmployedBillingProfile.name())
                .jsonPath("$.id").isEqualTo(selfEmployedBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(selfEmployedBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(selfEmployedBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(individualBillingProfile.name())
                .jsonPath("$.id").isEqualTo(individualBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(individualBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(individualBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        client.put()
                .uri(BILLING_PROFILES_TYPE_BY_ID.formatted(individualBillingProfile.id().value().toString()))
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
                .is5xxServerError();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_DELETE_BY_ID.formatted(individualBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    void should_put_and_get_billing_profile_payout_infos() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                faker.internet().url(), false);
        final SelfEmployedBillingProfile selfEmployedBillingProfile =
                billingProfileService.createSelfEmployedBillingProfile(UserId.of(authenticatedUser.user().getId()),
                        faker.rickAndMorty().character(), null);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": null,
                          "bankAccount": null,
                          "missingBankAccount": null,
                          "ethWallet": null,
                          "missingEthWallet": null,
                          "optimismAddress": null,
                          "missingOptimismWallet": null,
                          "aptosAddress": null,
                          "missingAptosWallet": null,
                          "starknetAddress": null,
                          "missingStarknetWallet": null
                        }""");

        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "ethWallet": "vitalik.eth",
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "bankAccount": {
                            "bic": "DAAEFRPPCCT",
                            "number": "FR5417569000301995586997O41"
                          },
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": null,
                          "bankAccount": {
                            "bic": "DAAEFRPPCCT",
                            "number": "FR5417569000301995586997O41"
                          },
                          "missingBankAccount": null,
                          "ethWallet": "vitalik.eth",
                          "missingEthWallet": null,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "missingOptimismWallet": null,
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "missingAptosWallet": null,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": null
                        }""");

        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": null,
                          "ethWallet": null,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "bankAccount": null,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": null,
                          "bankAccount": null,
                          "missingBankAccount": null,
                          "ethWallet": null,
                          "missingEthWallet": null,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "missingOptimismWallet": null,
                          "aptosAddress": null,
                          "missingAptosWallet": null,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": null
                        }""");
    }

    @Test
    void should_delete_manually_associated_billing_profile() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var kaaper = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");
        final var firstAdminId = UserId.of(antho.user().getId());

        final var billingProfile = billingProfileService.createCompanyBillingProfile(firstAdminId, faker.rickAndMorty().character(), null);
        payoutPreferenceFacadePort.setPayoutPreference(kaaper, billingProfile.id(), firstAdminId);

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_BY_ID.formatted(billingProfile.id().toString())))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC")
                ))
                .header("Authorization", "Bearer " + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    void should_delete_automatically_associated_billing_profile() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var kaaper = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");
        final var firstAdminId = UserId.of(antho.user().getId());

        final var billingProfile = billingProfileService.createCompanyBillingProfile(firstAdminId, faker.rickAndMorty().character(), Set.of(kaaper));

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_BY_ID.formatted(billingProfile.id().toString())))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC")
                ))
                .header("Authorization", "Bearer " + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }
}
