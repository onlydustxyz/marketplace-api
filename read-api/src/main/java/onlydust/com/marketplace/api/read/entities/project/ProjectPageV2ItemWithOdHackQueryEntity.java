package onlydust.com.marketplace.api.read.entities.project;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponseV2;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponseV2OdHackStats;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Immutable
@Entity
public class ProjectPageV2ItemWithOdHackQueryEntity extends BaseProjectPageV2ItemQueryEntity {
    Integer odHackIssueCount;
    Integer odHackAvailableIssueCount;

    public ProjectShortResponseV2 toShortResponse() {
        return super.toShortResponse()
                .odHackStats(odHackStats());
    }

    private ProjectShortResponseV2OdHackStats odHackStats() {
        return odHackIssueCount == null || odHackAvailableIssueCount == null ? null : new ProjectShortResponseV2OdHackStats()
                .issueCount(odHackIssueCount)
                .availableIssueCount(odHackAvailableIssueCount);
    }
}
