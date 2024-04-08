package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.SponsorMapper;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.view.backoffice.BoSponsorView;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "sponsors", schema = "public")
public class SponsorEntity {

    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "logo_url", nullable = false)
    String logoUrl;
    @Column(name = "url")
    String url;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "sponsorId")
    Set<ProjectSponsorEntity> projects;

    public SponsorView toView() {
        return SponsorView.builder()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .projects(isNull(projects) ? List.of() : projects.stream().map(e -> e.project().toView()).toList())
                .build();
    }

    public Sponsor toDomain() {
        return Sponsor.builder()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .build();
    }

    public BoSponsorView toBoView() {
        return new BoSponsorView(
                id,
                name,
                url,
                logoUrl,
                projects.stream().map(SponsorMapper::mapToSponsor).collect(toSet()));
    }
}
