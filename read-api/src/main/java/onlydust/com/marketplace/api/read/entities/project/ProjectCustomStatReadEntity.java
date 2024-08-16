package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
public class ProjectCustomStatReadEntity {
    @Id
    @NonNull
    UUID projectId;

    int mergedPrCount;
    int activeContributorCount;
    int rewardCount;
}
