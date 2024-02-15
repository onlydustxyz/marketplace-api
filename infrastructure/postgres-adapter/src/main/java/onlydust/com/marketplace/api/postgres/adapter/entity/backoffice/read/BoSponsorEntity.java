package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.*;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "projects_sponsors", joinColumns = @JoinColumn(name = "sponsor_id"), inverseJoinColumns = @JoinColumn(name = "project_id"))
    @Builder.Default
    Set<ProjectIdEntity> projects = new HashSet<>();

    public SponsorView toView() {
        return SponsorView.builder().id(id).name(name).url(url).logoUrl(logoUrl).projectIds(projects != null ?
                projects.stream().map(ProjectIdEntity::getId).toList() : List.of()).build();
    }
}
