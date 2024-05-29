package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.CommitteeProjectInfosResponse;
import onlydust.com.marketplace.api.contract.model.CommitteeStatus;
import onlydust.com.marketplace.api.contract.model.ProjectLast3MonthsMetricsResponse;
import onlydust.com.marketplace.api.contract.model.RegisteredUserResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectInfosQueryEntity;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

public interface CommitteeMapper {
    static CommitteeStatus map(final @NonNull Committee.Status status) {
        return switch (status) {
            case DRAFT -> throw OnlyDustException.internalServerError("Committee status DRAFT is not allowed here");
            case CLOSED -> CommitteeStatus.CLOSED;
            case OPEN_TO_VOTES -> CommitteeStatus.OPEN_TO_VOTES;
            case OPEN_TO_APPLICATIONS -> CommitteeStatus.OPEN_TO_APPLICATIONS;
        };
    }

    static Optional<BigDecimal> averageScoreOf(final @NonNull List<CommitteeJuryVoteViewEntity> votes) {
        return votes.stream()
                .filter(vote -> vote.getScore() != null)
                .mapToInt(CommitteeJuryVoteViewEntity::getScore)
                .average().stream()
                .mapToObj(BigDecimal::valueOf)
                .map(bigDecimal -> bigDecimal.setScale(1, RoundingMode.HALF_UP))
                .findFirst();
    }

    static CommitteeProjectInfosResponse map(final @NonNull ProjectInfosQueryEntity project) {
        return new CommitteeProjectInfosResponse()
                .id(project.id())
                .name(project.name())
                .slug(project.slug())
                .logoUrl(isNull(project.logoUrl()) ? null : project.logoUrl().toString())
                .shortDescription(project.shortDescription())
                .projectLeads(project.projectLeads().stream()
                        .map(projectLead -> new RegisteredUserResponse()
                                .id(projectLead.id())
                                .githubUserId(projectLead.githubId())
                                .avatarUrl(projectLead.avatarUrl())
                                .login(projectLead.login())
                        ).toList())
                .longDescription(project.longDescription())
                .last3monthsMetrics(
                        new ProjectLast3MonthsMetricsResponse()
                                .activeContributors(project.activeContributors())
                                .amountSentInUsd(project.amountSentInUsd())
                                .contributorsRewarded(project.contributorsRewarded())
                                .contributionsCompleted(project.contributionsCompleted())
                                .newContributors(project.newContributors())
                                .openIssues(project.openIssue())
                );
    }
}
