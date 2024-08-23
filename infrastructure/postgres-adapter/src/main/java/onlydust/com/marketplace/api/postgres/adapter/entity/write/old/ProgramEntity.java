package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Program;

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
    String logoUrl;

    public static ProgramEntity of(Program program) {
        return ProgramEntity.builder()
                .id(program.id().value())
                .name(program.name())
                .logoUrl(program.logoUrl().toString())
                .build();
    }
}
