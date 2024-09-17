package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsor_accounts", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class SponsorAccountReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @OneToOne
    @NonNull
    CurrencyReadEntity currency;

    @NonNull
    UUID sponsorId;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sponsorId", insertable = false, updatable = false)
    SponsorReadEntity sponsor;

    ZonedDateTime lockedUntil;

    @OneToMany(mappedBy = "account")
    @OrderBy("timestamp DESC")
    @NonNull
    List<SponsorAccountTransactionReadEntity> transactions;
}
