package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
@Accessors(chain = true, fluent = true)
@Data
public class BatchPayment {
    @NonNull
    Id id;
    @NonNull
    String csv;
    @NonNull
    Network network;
    @NonNull
    @Builder.Default
    Status status = Status.TO_PAY;
    String transactionHash;
    @NonNull
    List<Invoice> invoices;
    @NonNull
    List<PayableReward> rewards;
    Date createdAt;

    public static BatchPayment of(@NonNull Network network, @NonNull List<PayableReward> rewards, @NonNull String csv) {
        return BatchPayment.builder()
                .id(Id.random())
                .network(network)
                .rewards(rewards)
                .invoices(List.of())
                .createdAt(new Date())
                .csv(csv)
                .build();
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static BatchPayment.Id of(@NonNull final UUID uuid) {
            return BatchPayment.Id.builder().uuid(uuid).build();
        }

        public static BatchPayment.Id of(@NonNull final String uuid) {
            return BatchPayment.Id.of(UUID.fromString(uuid));
        }
    }


    public enum Status {
        TO_PAY, PAID
    }
}
