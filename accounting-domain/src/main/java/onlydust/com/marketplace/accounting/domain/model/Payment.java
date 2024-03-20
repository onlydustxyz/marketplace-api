package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.*;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Builder(toBuilder = true)
@Accessors(chain = true, fluent = true)
@Data
public class Payment {
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

    @Builder.Default
    final @NonNull Map<RewardId, Reference> references = new HashMap<>();

    public static Payment of(@NonNull Network network, @NonNull List<PayableReward> rewards, @NonNull String csv) {
        return Payment.builder()
                .id(Id.random())
                .network(network)
                .rewards(rewards)
                .invoices(List.of())
                .createdAt(new Date())
                .csv(csv)
                .build();
    }

    public Reference referenceFor(@NonNull RewardId rewardId) {
        return references.computeIfAbsent(rewardId, this::computeReference);
    }

    private Reference computeReference(RewardId rewardId) {
        final var invoice = invoices.stream()
                .filter(i -> i.rewards().stream().anyMatch(reward -> reward.id().equals(rewardId)))
                .findFirst()
                .orElseThrow(() -> internalServerError("Reward %s not found in batch payment %s".formatted(rewardId, id)));

        final var wallet = invoice.billingProfileSnapshot().wallet(network)
                .orElseThrow(() -> internalServerError("Wallet not found for invoice %s on network %s".formatted(invoice.id(), network)));

        return new Reference(network, transactionHash, invoice.billingProfileSnapshot().subject(), wallet.address());
    }

    public void referenceFor(@NonNull RewardId rewardId, Reference reference) {
        references.put(rewardId, reference);
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
        private final @NonNull Network network;
        private final @NonNull String reference;
        private final @NonNull String thirdPartyName;
        private final @NonNull String thirdPartyAccountNumber;
    }

    public enum Status {
        TO_PAY, PAID
    }
}
