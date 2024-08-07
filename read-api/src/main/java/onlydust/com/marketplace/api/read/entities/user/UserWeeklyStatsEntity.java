package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.UserWeeklyStats;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class UserWeeklyStatsEntity {
    @Id
    @EqualsAndHashCode.Include
    ZonedDateTime createdAt;

    Integer codeReviewCount;
    Integer issueCount;
    Integer pullRequestCount;
    Integer rewardCount;

    public UserWeeklyStats toDto() {
        return new UserWeeklyStats()
                .year(createdAt.getYear())
                .week(createdAt.get(WeekFields.ISO.weekOfWeekBasedYear()))
                .codeReviewCount(codeReviewCount)
                .issueCount(issueCount)
                .pullRequestCount(pullRequestCount)
                .rewardCount(rewardCount)
                ;
    }
}
