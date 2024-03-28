package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

public record Receipt(@NonNull Id id, @NonNull RewardId rewardId, @NonNull ZonedDateTime createdAt,
                      @NonNull Network network, @NonNull String reference, @NonNull String thirdPartyName, @NonNull String thirdPartyAccountNumber) {
    public static Receipt of(@NonNull final RewardId rewardId, @NonNull final Payment.Reference reference) {
        return new Receipt(Id.random(),
                rewardId,
                ZonedDateTime.now(),
                reference.network(),
                reference.reference(),
                reference.thirdPartyName(),
                reference.thirdPartyAccountNumber());
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }
}