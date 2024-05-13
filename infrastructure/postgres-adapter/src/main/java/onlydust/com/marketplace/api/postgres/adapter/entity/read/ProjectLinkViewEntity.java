package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Table(name = "projects", schema = "public")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProjectLinkViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    @NonNull String slug;
    @NonNull String name;
    @Column(name = "logo_url")
    @NonNull String logoUrl;
}
