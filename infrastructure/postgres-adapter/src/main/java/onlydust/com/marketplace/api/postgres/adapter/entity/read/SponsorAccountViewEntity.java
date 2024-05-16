package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Table(name = "sponsor_accounts", schema = "accounting")
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

