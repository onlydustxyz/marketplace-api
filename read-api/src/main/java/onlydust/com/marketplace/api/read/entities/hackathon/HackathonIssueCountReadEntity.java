package onlydust.com.marketplace.api.read.entities.hackathon;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "hackathon_issue_counts", schema = "public")
@Immutable
public class HackathonIssueCountReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID hackathonId;
    @NonNull
    Integer issueCount;
    @NonNull
    Integer openIssueCount;
}
