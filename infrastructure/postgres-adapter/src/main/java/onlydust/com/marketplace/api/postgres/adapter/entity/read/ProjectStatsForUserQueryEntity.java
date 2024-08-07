package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class ProjectStatsForUserQueryEntity {

    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "slug")
    String slug;
    @Column(name = "is_lead")
    Boolean isLead;
    @Column(name = "lead_since")
    Date leadSince;
    @Column(name = "name")
    String name;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "contributors_count")
    Integer contributorsCount;
    @Column(name = "total_granted")
    BigDecimal totalGranted;
    @Column(name = "user_contributions_count")
    Integer userContributionsCount;
    @Column(name = "last_contribution_date")
    Date lastContributionDate;
    @Column(name = "first_contribution_date")
    Date firstContributionDate;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibility visibility;
}
