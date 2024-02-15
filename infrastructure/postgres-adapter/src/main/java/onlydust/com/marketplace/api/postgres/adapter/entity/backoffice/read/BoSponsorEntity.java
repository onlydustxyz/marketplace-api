package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.*;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Builder(toBuilder = true)
@Table(name = "sponsors", schema = "public")
public class BoSponsorEntity {
    @Id
    @NonNull UUID id;
    @NonNull String name;
    String url;
    @Column(name = "logo_url")
    @NonNull String logoUrl;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "sponsorId")
    @Builder.Default
    Set<ProjectSponsorEntity> projects = new HashSet<>();

    public SponsorView toView() {
        final var sponsorView = new SponsorView(id, name, url, logoUrl);
        projects.forEach(p -> sponsorView.addProjectId(p.getProjectId(), ZonedDateTime.ofInstant(p.getLastAllocationDate().toInstant(), ZoneOffset.UTC)));
        return sponsorView;
    }
}
