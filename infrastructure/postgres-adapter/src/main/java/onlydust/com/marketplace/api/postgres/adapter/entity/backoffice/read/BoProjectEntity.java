package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "visibility", typeClass = PostgreSQLEnumType.class)
public class BoProjectEntity {
    @Id
    UUID id;
    String name;
    String shortDescription;
    String longDescription;
    @Type(type = "jsonb")
    List<String> moreInfoLinks;
    String logoUrl;
    Boolean hiring;
    Integer rank;
    @Enumerated(EnumType.STRING)
    @Type(type = "visibility")
    ProjectVisibility visibility;
    @Type(type = "jsonb")
    List<UUID> projectLeadIds;
    ZonedDateTime createdAt;
    Long activeContributors;
    Long newContributors;
    Long uniqueRewardedContributors;
    Long openedIssues;
    Long contributions;
    BigDecimal dollarsEquivalentAmountSent;
    BigDecimal strkAmountSent;

    public ProjectView toView() {
        return ProjectView.builder()
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
