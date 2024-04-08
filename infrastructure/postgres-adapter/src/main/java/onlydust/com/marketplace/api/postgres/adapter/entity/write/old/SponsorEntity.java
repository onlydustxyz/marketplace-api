package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "projects_sponsors",
            joinColumns = @JoinColumn(name = "sponsor_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    Set<ProjectEntity> projects;

    public SponsorView toView() {
        return SponsorView.builder()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .projects(isNull(projects) ? List.of() : projects.stream().map(ProjectEntity::toView).toList())
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
}
