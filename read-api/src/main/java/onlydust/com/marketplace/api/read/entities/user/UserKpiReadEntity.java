package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ContributorKpiResponse;
import org.hibernate.annotations.Immutable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "all_users", schema = "iam")
public class UserKpiReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    Integer maintainedProjects;
    Integer contributedProjects;
    Integer rewards;
    Integer mergedPRs;
    Integer resolvedIssues;
    Integer pendingApplications;

    public ContributorKpiResponse toResponse() {
        return new ContributorKpiResponse()
                .contributedProjects(contributedProjects)
                .maintainedProjects(maintainedProjects)
                .rewards(rewards)
                .mergedPRs(mergedPRs)
                .resolvedIssues(resolvedIssues)
                .pendingApplications(pendingApplications);
    }
}
