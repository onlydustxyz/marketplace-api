package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ContributorActivityGraphDayResponse;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@IdClass(ContributorActivityGraphDayReadEntity.PrimaryKey.class)
public class ContributorActivityGraphDayReadEntity {
    @Id
    @NonNull
    Integer day;
    @Id
    @NonNull
    Integer year;
    @NonNull
    Integer week;

    @NonNull
    Integer rewardCount;
    @NonNull
    Integer issueCount;
    @NonNull
    Integer prCount;
    @NonNull
    Integer codeReviewCount;

    public ContributorActivityGraphDayResponse toDto() {
        return new ContributorActivityGraphDayResponse()
                .day(day)
                .year(year)
                .week(week)
                .rewardCount(rewardCount)
                .issueCount(issueCount)
                .pullRequestCount(prCount)
                .codeReviewCount(codeReviewCount);
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Integer day;
        Integer year;
    }
}
