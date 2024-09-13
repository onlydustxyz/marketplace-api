package onlydust.com.marketplace.api.postgres.adapter.ut;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NotificationEntityTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    }
}
