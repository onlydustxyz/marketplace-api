package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Accessors(fluent = true)
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Immutable
@Table(name = "projects", schema = "public")
public class ProjectEcosystemCardReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    String name;
    @NonNull
    String slug;
    @NonNull
    String shortDescription;
    String logoUrl;





}
