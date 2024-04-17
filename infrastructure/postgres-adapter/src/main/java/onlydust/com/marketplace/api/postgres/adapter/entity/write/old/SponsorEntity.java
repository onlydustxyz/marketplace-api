package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "sponsors", schema = "public")
public class SponsorEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String name;
    @NonNull
    String logoUrl;
    String url;
}
