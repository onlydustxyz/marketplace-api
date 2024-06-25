package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "receipts", schema = "accounting")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true)
@Immutable
public class ReceiptReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    Date createdAt;

    @Enumerated(EnumType.STRING)
    @NonNull
    NetworkEnumEntity network;

    @NonNull
    String thirdPartyName;
    @NonNull
    String thirdPartyAccountNumber;
    @NonNull
    String transactionReference;
}
