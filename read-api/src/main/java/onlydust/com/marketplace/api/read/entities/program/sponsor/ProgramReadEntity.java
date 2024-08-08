package onlydust.com.marketplace.api.read.entities.program.sponsor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProgramPageItemResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsors", schema = "public") //  TODO change to programs after migration
@Immutable
@Accessors(fluent = true)
public class ProgramReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String name;

    public ProgramPageItemResponse toPageItemResponse() {
        return new ProgramPageItemResponse()
                .id(id)
                .name(name);
    }
}
