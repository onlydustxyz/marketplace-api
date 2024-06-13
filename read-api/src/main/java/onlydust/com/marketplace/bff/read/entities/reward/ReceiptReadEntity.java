package onlydust.com.marketplace.bff.read.entities.reward;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.UUID;

@Entity
@Value
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
