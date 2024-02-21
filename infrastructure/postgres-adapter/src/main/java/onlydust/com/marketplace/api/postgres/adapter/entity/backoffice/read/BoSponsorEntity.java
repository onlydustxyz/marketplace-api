package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.*;
import onlydust.com.marketplace.project.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.SponsorMapper;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return new SponsorView(id, name, url, logoUrl,
                projects.stream().map(SponsorMapper::mapToSponsor).collect(Collectors.toSet()));
    }
}
