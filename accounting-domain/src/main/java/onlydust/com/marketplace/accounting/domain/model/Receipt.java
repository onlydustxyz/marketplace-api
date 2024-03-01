package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
@Accessors(fluent = true)
public class Receipt {
    @NonNull Id id;
    @NonNull RewardId rewardId;
    @NonNull ZonedDateTime createdAt;
    @NonNull Network network;
    @NonNull String reference;
    @NonNull String thirdPartyName;
    @NonNull String thirdPartyAccountNumber;

    public static Receipt of(@NonNull final RewardId rewardId, @NonNull final SponsorAccount.PaymentReference reference) {
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