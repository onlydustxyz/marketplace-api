package onlydust.com.marketplace.api.postgres.adapter.ut;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.notification.BillingProfileVerificationRejected;
import onlydust.com.marketplace.accounting.domain.notification.CompleteYourBillingProfile;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.project.domain.model.notification.*;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NotificationEntityTest {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Nested
    class Serialization {
        @SneakyThrows
        @Test
        void should_deserialize_id_from_uuid_wrapper() {
            // Given
            @Language("JSON") final var data = """
                    {
                      "notification": {
                        "@type": "RewardCanceled",
                        "shortReward": {
                          "id": {
                            "uuid": "87afc209-aab4-409e-b3d9-f7119cff15e6"
                          },
                          "amount": 273.04902292628555,
                          "projectName": "kaaper",
                          "currencyCode": "WLD",
                          "dollarsEquivalent": 500.000000000000003405871859793260
                        }
                      }
                    }
                    """;

            // When
            final var notification = (RewardCanceled) objectMapper.readValue(data, NotificationEntity.Data.class).notification();

            // Then
            assertThat(notification.shortReward().id().toString()).isEqualTo("87afc209-aab4-409e-b3d9-f7119cff15e6");
        }

        @SneakyThrows
        @Test
        void should_deserialize_id_from_string() {
            // Given
            @Language("JSON") final var data = """
                    {
                      "notification": {
                        "@type": "RewardCanceled",
                        "shortReward": {
                          "id": "87afc209-aab4-409e-b3d9-f7119cff15e6",
                          "amount": 273.04902292628555,
                          "projectName": "kaaper",
                          "currencyCode": "WLD",
                          "dollarsEquivalent": 500.000000000000003405871859793260
                        }
                      }
                    }
                    """;

            // When
            final var notification = (RewardCanceled) objectMapper.readValue(data, NotificationEntity.Data.class).notification();

            // Then
            assertThat(notification.shortReward().id().toString()).isEqualTo("87afc209-aab4-409e-b3d9-f7119cff15e6");
        }

        @SneakyThrows
        @Test
        void should_deserialize_notifications_from_string() {
            // @formatter:off
            final var notifications = Map.of(
                    ApplicationAccepted.class, "{\"notification\": {\"@type\": \"ApplicationAccepted\", \"issue\": {\"id\": 2477871635, \"title\": \"Test\", \"htmlUrl\": \"https://github.com/PaulDemOD/test/issues/3\", \"repoName\": \"test\", \"description\": \"Test\"}, \"project\": {\"id\": \"9f4ab56c-9d24-479b-94e2-326b508c2fe9\", \"name\": \"noir\", \"slug\": \"noir\"}}}",
                    BillingProfileVerificationRejected.class, "{\"notification\": {\"@type\": \"BillingProfileVerificationRejected\", \"rejectionReason\": \"The photo displays signs of tampering via a digital editor. The purpose of the editor could be anything from resizing to forgery.\", \"billingProfileId\": {\"uuid\": \"c0b6b02c-606c-4721-beb1-01627b054779\"}, \"billingProfileName\": \"Paul Demarecaux\"}}",
                    CompleteYourBillingProfile.class, "{\"notification\": {\"@type\": \"CompleteYourBillingProfile\", \"billingProfile\": {\"billingProfileId\": \"01d19bbf-ef25-47f8-ab63-cfcaa89e828d\", \"billingProfileName\": \"fake\", \"verificationStatus\": \"STARTED\"}}}",
                    DepositApproved.class, "{\"notification\": {\"@type\": \"DepositApproved\", \"amount\": 10.000000, \"depositId\": \"f9f8edec-5f48-4f2d-936d-f769ca3dbbc2\", \"sponsorId\": {\"uuid\": \"58a0a05c-c81e-447c-910f-629817a987b8\"}, \"timestamp\": 1726241880.000000000, \"currencyId\": \"e083f069-d6e3-4516-be0c-f89b1a8098f9\"}}",
                    DepositRejected.class, "{\"notification\": {\"@type\": \"DepositRejected\", \"amount\": 12.000000, \"depositId\": \"4c79ddd3-ea07-4295-bd65-3f910b500c91\", \"sponsorId\": {\"uuid\": \"58a0a05c-c81e-447c-910f-629817a987b8\"}, \"timestamp\": 1726243752.000000000, \"currencyId\": \"e083f069-d6e3-4516-be0c-f89b1a8098f9\"}}",
                    FundsAllocatedToProgram.class, "{\"notification\": {\"@type\": \"FundsAllocatedToProgram\", \"amount\": 1, \"programId\": {\"uuid\": \"37ec499e-7aa2-49bd-9dd7-1588f07c063e\"}, \"sponsorId\": {\"uuid\": \"58a0a05c-c81e-447c-910f-629817a987b8\"}, \"currencyId\": \"e083f069-d6e3-4516-be0c-f89b1a8098f9\"}}",
                    GoodFirstIssueCreated.class, "{\"notification\": {\"@type\": \"GoodFirstIssueCreated\", \"issue\": {\"id\": 2489943353, \"title\": \"GFI #6\", \"labels\": [\"good first issue\"], \"htmlUrl\": \"https://github.com/onlydustxyz/marketplace-api/issues/1063\", \"repoName\": \"marketplace-api\", \"authorLogin\": \"PierreOucif\", \"description\": null, \"authorAvatarUrl\": \"https://avatars.githubusercontent.com/u/16590657?v=4\"}, \"project\": {\"id\": \"d1615adc-2738-4181-aa9f-29cdad65b638\", \"name\": \"Onlydust\", \"slug\": \"onlydust\"}}}",
                    ApplicationToReview.class, "{\"notification\": {\"user\": {\"id\": \"60acca26-cb91-4b41-9ed3-f7b9cef0859e\", \"login\": \"PaulDemOD\", \"githubId\": 171552874}, \"@type\": \"ProjectApplicationToReview\", \"issue\": {\"id\": 2489943628, \"title\": \"GFI #7\", \"htmlUrl\": \"https://github.com/onlydustxyz/marketplace-api/issues/1064\", \"repoName\": \"marketplace-api\", \"description\": null}, \"project\": {\"id\": \"7d04163c-4187-4313-8066-61504d34fc56\", \"name\": \"Bretzel\", \"slug\": \"bretzel\"}}}",
                    RewardCanceled.class, "{\"notification\": {\"@type\": \"RewardCanceled\", \"shortReward\": {\"id\": {\"uuid\": \"8581eb4b-d8ec-414f-9e63-541cf840f498\"}, \"amount\": 1, \"projectName\": \"Bretzel\", \"currencyCode\": \"LORDS\", \"dollarsEquivalent\": 0.047694476556112975, \"sentByGithubLogin\": \"PierreOucif\", \"contributionsCount\": 1}}}",
                    RewardReceived.class, "{\"notification\": {\"@type\": \"RewardReceived\", \"shortReward\": {\"id\": {\"uuid\": \"a98426ce-ba51-4f4e-b6a4-2ea854e8d718\"}, \"amount\": 253.3279621220396, \"projectName\": \"Bretzel\", \"currencyCode\": \"LORDS\", \"dollarsEquivalent\": null, \"sentByGithubLogin\": \"PaulDemOD\", \"contributionsCount\": 1}, \"contributionCount\": 1, \"sentByGithubLogin\": \"PaulDemOD\"}}"
            );
            // @formatter:on

            for (final var entry : notifications.entrySet()) {
                final var notification = objectMapper.readValue(entry.getValue(), NotificationEntity.Data.class).notification();
                assertThat(notification).isInstanceOf(entry.getKey());
            }
        }
    }
}
