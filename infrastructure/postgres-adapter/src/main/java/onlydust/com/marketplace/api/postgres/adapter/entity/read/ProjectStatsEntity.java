package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class ProjectStatsEntity {

    @Id
    @Column(name = "project_id")
    UUID id;
    @Column(name = "contributors_count")
    Integer contributorsCount;
    @Column(name = "total_granted")
    BigDecimal totalGranted;
    @Column(name = "user_contributions_count")
    Integer userContributionsCount;
    @Column(name = "last_contribution_date")
    Date lastContributionDate;
}
