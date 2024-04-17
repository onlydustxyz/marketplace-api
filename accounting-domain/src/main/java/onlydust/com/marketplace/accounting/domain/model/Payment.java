package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
@Accessors(chain = true, fluent = true)
@Data
public class Payment {
    @NonNull
    Id id;
    String csv;
    @NonNull
    Network network;
    @NonNull
    @Builder.Default
    Status status = Status.TO_PAY;
    String transactionHash;
    @EqualsAndHashCode.Exclude
    ZonedDateTime confirmedAt;
    @NonNull
    List<PayableReward> rewards;
    Date createdAt;

    public static Payment of(@NonNull Network network, @NonNull List<PayableReward> rewards) {
        return Payment.builder()
                .id(Id.random())
                .network(network)
                .rewards(rewards)
                .createdAt(new Date())
                .build();
    }

    public Reference referenceFor(@NonNull RewardId rewardId) {
        final var reward = rewards().stream().filter(r -> r.id().equals(rewardId)).findFirst().orElseThrow();
        return new Reference(confirmedAt, network, transactionHash, reward.recipientName(), reward.recipientWallet().address());
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Payment.Id of(@NonNull final UUID uuid) {
            return Payment.Id.builder().uuid(uuid).build();
        }

        public static Payment.Id of(@NonNull final String uuid) {
            return Payment.Id.of(UUID.fromString(uuid));
        }
    }

    @AllArgsConstructor
    @Accessors(fluent = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Reference {
        private final @NonNull ZonedDateTime timestamp;
        private final @NonNull Network network;
        private final @NonNull String reference;
        private final @NonNull String thirdPartyName;
        private final @NonNull String thirdPartyAccountNumber;
    }

    public enum Status {
        TO_PAY, PAID
    }
}
