package onlydust.com.marketplace.bff.read.entities.ecosystem;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemPageItemResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "ecosystems", schema = "public")
@Immutable
@Accessors(fluent = true)
public class EcosystemReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private UUID id;

    private @NonNull String name;

    public EcosystemPageItemResponse toPageItemResponse() {
        return new EcosystemPageItemResponse()
                .id(id)
                .slug(null) // TODO
                .name(name)
                .description(null) // TODO
                .banners(null) // TODO
                .projectCount(0) // TODO
                .topProjects(null) // TODO
                ;
    }
}
