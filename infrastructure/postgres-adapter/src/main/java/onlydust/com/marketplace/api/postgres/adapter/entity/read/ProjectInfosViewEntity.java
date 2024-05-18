package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.ProjectLeaderLinkView;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
public class ProjectInfosViewEntity {
    @Id
    UUID id;
    @NonNull
    String shortDescription;
    @NonNull
    String longDescription;
    @NonNull
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectPageItemQueryEntity.ProjectLead> projectLeads;
    @NonNull
    Integer activeContributors;
    @NonNull
    BigDecimal amountSentInUsd;
    @NonNull
    Integer contributorsRewarded;
    @NonNull
    Integer contributionsCompleted;
    @NonNull
    Integer newContributors;
    @NonNull
    Integer openIssue;

    public CommitteeApplicationView.ProjectInfosView toView() {
        return new CommitteeApplicationView.ProjectInfosView(
                shortDescription,
                longDescription,
                projectLeads.stream().map(projectLead -> ProjectLeaderLinkView.builder()
                                .id(projectLead.id)
                                .url(projectLead.url)
                                .githubUserId(projectLead.githubId)
                                .login(projectLead.login)
                                .avatarUrl(projectLead.avatarUrl)
                                .build())
                        .toList(),
                activeContributors,
                amountSentInUsd,
                contributorsRewarded,
                contributionsCompleted,
                newContributors,
                openIssue
        );
    }
}
