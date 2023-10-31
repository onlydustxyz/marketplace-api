package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ProjectStatsForUserEntity {

    @Id
    @Column(name = "project_id")
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
    @Type(type = "project_visibility")
    @Column(columnDefinition = "visibility")
    ProjectVisibilityEnumEntity visibility;
}
