package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Table(name = "sponsor_accounts", schema = "accounting")
@Builder(access = AccessLevel.PRIVATE)
@Immutable
public class SponsorAccountViewEntity {
    @Id
    @NonNull
    UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @NonNull
    CurrencyViewEntity currency;

    UUID sponsorId;

    Instant lockedUntil;
}

