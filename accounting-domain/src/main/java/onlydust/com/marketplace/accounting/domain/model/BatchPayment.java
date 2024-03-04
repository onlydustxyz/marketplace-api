package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.util.List;
import java.util.UUID;

@Builder
@Accessors(chain = true, fluent = true)
@Data
public class BatchPayment {
    @NonNull
    Id id;
    @NonNull
    String csv;
    @NonNull
    Long rewardCount;
    @NonNull
    Blockchain blockchain;
    @NonNull
    List<MoneyView> moneys;

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Invoice.Id of(@NonNull final UUID uuid) {
            return Invoice.Id.builder().uuid(uuid).build();
        }

        public static Invoice.Id of(@NonNull final String uuid) {
            return Invoice.Id.of(UUID.fromString(uuid));
        }
    }
}
