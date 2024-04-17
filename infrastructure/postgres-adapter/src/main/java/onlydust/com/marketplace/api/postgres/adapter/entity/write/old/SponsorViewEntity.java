package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.SponsorMapper;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.view.backoffice.BoSponsorView;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Entity
@NoArgsConstructor(force = true)
@Value
@Table(name = "sponsors", schema = "public")
public class SponsorViewEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String name;
    @NonNull
    String logoUrl;
    String url;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "sponsorId")
    Set<ProjectSponsorEntity> projects = Set.of();

    @ManyToMany
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
