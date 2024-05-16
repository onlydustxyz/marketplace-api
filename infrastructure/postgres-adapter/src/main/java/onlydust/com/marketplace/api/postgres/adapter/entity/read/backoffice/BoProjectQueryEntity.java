package onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.backoffice.OldProjectView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoProjectQueryEntity {
    @Id
    UUID id;
    String name;
    String shortDescription;
    String longDescription;
    @JdbcTypeCode(SqlTypes.JSON)
    List<String> moreInfoLinks;
    String logoUrl;
    Boolean hiring;
    Integer rank;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "visibility")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ProjectVisibility visibility;
    @JdbcTypeCode(SqlTypes.JSON)
    List<UUID> projectLeadIds;
    ZonedDateTime createdAt;
    Long activeContributors;
    Long newContributors;
    Long uniqueRewardedContributors;
    Long openedIssues;
    Long contributions;
    BigDecimal dollarsEquivalentAmountSent;
    BigDecimal strkAmountSent;

    public OldProjectView toView() {
        return OldProjectView.builder()
                .id(id)
                .name(name)
                .shortDescription(shortDescription)
                .longDescription(longDescription)
                .moreInfoLinks(moreInfoLinks)
                .logoUrl(logoUrl)
                .hiring(hiring)
                .rank(rank)
                .visibility(visibility)
                .projectLeadIds(projectLeadIds)
                .createdAt(createdAt)
                .activeContributors(activeContributors)
                .newContributors(newContributors)
                .uniqueRewardedContributors(uniqueRewardedContributors)
                .openedIssues(openedIssues)
                .contributions(contributions)
                .dollarsEquivalentAmountSent(dollarsEquivalentAmountSent)
                .strkAmountSent(strkAmountSent)
                .build();
    }
}
