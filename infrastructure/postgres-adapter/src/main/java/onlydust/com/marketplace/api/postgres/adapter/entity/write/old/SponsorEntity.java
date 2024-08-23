package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.util.UUID;

import static onlydust.com.marketplace.accounting.domain.model.SponsorId.of;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "sponsors", schema = "public")
public class SponsorEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String name;
    @NonNull
    String logoUrl;
    String url;

    public Sponsor toDomain() {
        return Sponsor.builder()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .url(url)
                .build();
    }

    public onlydust.com.marketplace.accounting.domain.view.Sponsor toAccountingDomain() {
        return onlydust.com.marketplace.accounting.domain.view.Sponsor.builder()
                .id(of(id))
                .name(name)
                .logoUrl(logoUrl)
                .build();
    }
}
