package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectEcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "project_details", schema = "public")
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
@DynamicUpdate
@SelectBeforeUpdate
public class ProjectEntity {
    @Id
    @Column(name = "project_id", nullable = false)
    UUID id;
    @Column(name = "name")
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "long_description")
    String longDescription;
    @Column(name = "telegram_link")
    String telegramLink;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "hiring")
    Boolean hiring;
    @Column(name = "rank", updatable = false)
    Integer rank;
    @Column(name = "key", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    String key;
    @Enumerated(EnumType.STRING)
    @Type(type = "project_visibility")
    @Column(columnDefinition = "visibility", updatable = false)
    ProjectVisibilityEnumEntity visibility;
    @Column(name = "reward_ignore_pull_requests_by_default")
    Boolean ignorePullRequests;
    @Column(name = "reward_ignore_issues_by_default")
    Boolean ignoreIssues;
    @Column(name = "reward_ignore_code_reviews_by_default")
    Boolean ignoreCodeReviews;
    @Column(name = "reward_ignore_contributions_before_date_by_default")
    Date ignoreContributionsBefore;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectLeaderInvitationEntity> projectLeaderInvitations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectLeadEntity> projectLeaders;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectRepoEntity> repos;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectMoreInfoEntity> moreInfos;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectEcosystemEntity> ecosystems;

}
