package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.mapper.SponsorMapper;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.view.backoffice.BoSponsorView;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsors", schema = "public")
@Immutable
public class SponsorViewEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String name;
    @NonNull
    String logoUrl;
    String url;

    @OneToMany(mappedBy = "sponsorId", fetch = FetchType.LAZY)
    Set<ProjectSponsorViewEntity> projects = Set.of();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sponsors_users",
            joinColumns = @JoinColumn(name = "sponsor_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    List<UserViewEntity> users;

    public SponsorView toView() {
        return SponsorView.builder()
                .id(SponsorId.of(id))
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .projects(projects.stream().map(e -> e.project().toView()).toList())
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
