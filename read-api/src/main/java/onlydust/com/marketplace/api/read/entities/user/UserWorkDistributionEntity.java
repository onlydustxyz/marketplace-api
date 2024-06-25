package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.UserWorkDistribution;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserWorkDistributionEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long contributorId;

    @NonNull
    Integer codeReviewCount;
    @NonNull
    Integer issueCount;
    @NonNull
    Integer pullRequestCount;

    public UserWorkDistribution toDto() {
        return new UserWorkDistribution()
                .codeReviewCount(codeReviewCount)
                .issueCount(issueCount)
                .pullRequestCount(pullRequestCount)
                ;
    }
}
