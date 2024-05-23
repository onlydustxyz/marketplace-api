package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.ProjectInfosView;
import onlydust.com.marketplace.project.domain.view.ProjectLeaderLinkView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Immutable
@Getter
public class ProjectInfosQueryEntity {
    @Id
    UUID id;
    @NonNull String slug;
    @NonNull String name;
    String logoUrl;
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
    @NonNull
    String visibility;

    public ProjectInfosView toView() {
        return new ProjectInfosView(
                id,
                name,
                slug,
                isNull(logoUrl) ? null : URI.create(logoUrl),
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
