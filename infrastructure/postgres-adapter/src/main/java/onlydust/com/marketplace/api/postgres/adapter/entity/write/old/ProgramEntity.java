package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
@Table(name = "programs", schema = "public")
public class ProgramEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String name;
    @NonNull
    UUID sponsorId;
    String url;
    String logoUrl;

    @OneToMany(mappedBy = "programId", cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    List<ProgramLeadEntity> leads;

    public static ProgramEntity of(Program program) {
        return ProgramEntity.builder()
                .id(program.id().value())
                .name(program.name())
                .sponsorId(program.sponsorId().value())
                .leads(program.leadIds().stream().map(userId -> ProgramLeadEntity.of(program.id(), userId)).toList())
                .url(Optional.ofNullable(program.url()).map(URI::toString).orElse(null))
                .logoUrl(Optional.ofNullable(program.logoUrl()).map(URI::toString).orElse(null))
                .build();
    }

    public Program toDomain() {
        return Program.builder()
                .id(ProgramId.of(id))
                .name(name)
                .sponsorId(SponsorId.of(sponsorId))
                .leadIds(leads.stream().map(lead -> UserId.of(lead.userId())).toList())
                .url(Optional.ofNullable(url).map(URI::create).orElse(null))
                .logoUrl(Optional.ofNullable(logoUrl).map(URI::create).orElse(null))
                .build();
    }
}
