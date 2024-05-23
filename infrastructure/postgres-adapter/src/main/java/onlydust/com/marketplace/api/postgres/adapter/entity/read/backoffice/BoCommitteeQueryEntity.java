package onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoCommitteeQueryEntity {

    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    Date applicationStartDate;
    @NonNull
    Date applicationEndDate;
    @NonNull
    String name;
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "committee_status")
    CommitteeStatusEntity status;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<ProjectQuestionJson> projectQuestions;
    UUID sponsorId;
    String sponsorName;
    String sponsorUrl;
    String sponsorLogoUrl;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<ProjectApplicationLinkJson> projectApplications;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<JuryLinkJson> juries;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<JuryCriteriaJson> juryCriteria;

    public CommitteeView toView() {
        return CommitteeView.builder()
                .id(Committee.Id.of(this.id))
                .name(this.name)
                .status(this.status.toDomain())
                .applicationStartDate(ZonedDateTime.ofInstant(applicationStartDate.toInstant(), ZoneOffset.UTC))
                .applicationEndDate(ZonedDateTime.ofInstant(applicationEndDate.toInstant(), ZoneOffset.UTC))
                .projectQuestions(isNull(this.projectQuestions) ? List.of() : this.projectQuestions.stream()
                        .map(projectQuestionEntity -> new ProjectQuestion(ProjectQuestion.Id.of(projectQuestionEntity.getId()),
                                projectQuestionEntity.getQuestion(),
                                projectQuestionEntity.getRequired())).toList())
                .sponsor(isNull(this.sponsorName) ? null : new ShortSponsorView(this.sponsorId, this.sponsorName, this.sponsorUrl, this.sponsorLogoUrl))
                .committeeApplicationLinks(isNull(this.projectApplications) ? null : this.projectApplications.stream()
                        .map(projectApplicationLinkJson -> CommitteeApplicationLinkView.builder()
                                .projectShortView(ProjectShortView.builder()
                                        .slug(projectApplicationLinkJson.projectSlug)
                                        .shortDescription(projectApplicationLinkJson.projectShortDescription)
                                        .logoUrl(projectApplicationLinkJson.projectLogoUrl)
                                        .id(projectApplicationLinkJson.projectId)
                                        .name(projectApplicationLinkJson.projectName)
                                        .visibility(ProjectVisibility.valueOf(projectApplicationLinkJson.projectVisibility))
                                        .build()
                                )
                                .applicant(ProjectLeaderLinkView.builder()
                                        .avatarUrl(projectApplicationLinkJson.userAvatarUrl)
                                        .login(projectApplicationLinkJson.userGithubLogin)
                                        .githubUserId(projectApplicationLinkJson.userGithubId)
                                        .id(projectApplicationLinkJson.userId)
                                        .build())
                                .build()).toList())
                .juries(isNull(this.juries) ? null : this.juries.stream()
                        .map(juryLinkJson -> RegisteredContributorLinkView.builder()
                                .avatarUrl(juryLinkJson.userAvatarUrl)
                                .login(juryLinkJson.userGithubLogin)
                                .githubUserId(juryLinkJson.userGithubId)
                                .id(juryLinkJson.userId)
                                .build()
                        ).toList())
                .juryCriteria(isNull(this.juryCriteria) ? null : this.juryCriteria.stream()
                        .map(juryCriteriaJson -> new JuryCriteria(JuryCriteria.Id.of(juryCriteriaJson.id), juryCriteriaJson.criteria)).toList())
                .build();
    }

    @Data
    public static class ProjectQuestionJson {
        @NonNull
        UUID id;
        @NonNull
        String question;
        @NonNull
        Boolean required;
    }

    @Data
    public static class ProjectApplicationLinkJson {
        UUID userId;
        UUID projectId;
        String projectName;
        Long userGithubId;
        String projectSlug;
        String userAvatarUrl;
        String userGithubLogin;
        String projectLogoUrl;
        String projectShortDescription;
        String projectVisibility;
    }

    @Data
    public static class JuryLinkJson {
        UUID userId;
        Long userGithubId;
        String userAvatarUrl;
        String userGithubLogin;
    }

    @Data
    public static class JuryCriteriaJson {
        @NonNull
        UUID id;
        @NonNull
        String criteria;
    }

}
