package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadCommitteesApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeLinkViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeProjectAnswerViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectInfosViewRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.mapper.CommitteeMapper;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import onlydust.com.marketplace.project.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadCommitteesApiPostgresAdapter implements ReadCommitteesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final CommitteeLinkViewRepository committeeLinkViewRepository;
    private final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;
    private final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository;

    @Override
    public ResponseEntity<MyCommitteeAssignmentsResponse> getCommitteeAssignments(UUID committeeId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var userVotesForCommittee = committeeJuryVoteViewRepository.findAllByCommitteeIdAndUserId(committeeId, authenticatedUser.getId());
        if (userVotesForCommittee.isEmpty()) {
            throw notFound("No assignement found for user %s on committee %s".formatted(authenticatedUser.getId(), committeeId));
        }
        final var committee = committeeLinkViewRepository.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s not found".formatted(committeeId)));

        final Map<UUID, Double> averageScorePerProjects = userVotesForCommittee.stream()
                .filter(vote -> vote.getScore() != null)
                .collect(Collectors.groupingBy(CommitteeJuryVoteViewEntity::getProjectId, Collectors.averagingInt(CommitteeJuryVoteViewEntity::getScore)));

        final Map<UUID, List<CommitteeJuryVoteViewEntity>> votesMappedToProjectId = userVotesForCommittee.stream()
                .collect(Collectors.groupingBy(CommitteeJuryVoteViewEntity::getProjectId));

        return ResponseEntity.ok(new MyCommitteeAssignmentsResponse()
                .name(committee.getName())
                .status(CommitteeMapper.map(committee.getStatus()))
                .projectAssignments(votesMappedToProjectId.keySet()
                        .stream()
                        .map(projectId -> new CommitteeAssignmentLinkResponse()
                                .project(ProjectMapper.projectToResponse(votesMappedToProjectId.get(projectId).get(0).getProject()))
                                .score(Optional.ofNullable(averageScorePerProjects.get(projectId)).map(BigDecimal::valueOf)
                                        .map(bigDecimal -> bigDecimal.setScale(2, RoundingMode.HALF_UP))
                                        .orElse(null))).toList()));
    }


    @Override
    public ResponseEntity<MyCommitteeAssignmentResponse> getCommitteeAssignmentOnProject(UUID committeeId, UUID projectId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var votes = committeeJuryVoteViewRepository.findAllByCommitteeIdAndProjectIdAndUserId(committeeId, projectId, authenticatedUser.getId());
        if (votes.isEmpty()) {
            throw notFound("No assignement found for user %s on committee %s and project %s".formatted(authenticatedUser.getId(), committeeId, projectId));
        }
        final var project = projectInfosViewRepository.findByProjectId(projectId)
                .orElseThrow(() -> notFound("Project %s not found".formatted(projectId)));
        final var projectAnswers = committeeProjectAnswerViewRepository.findByCommitteeIdAndAndProjectId(committeeId, projectId);

        final MyCommitteeAssignmentResponse myCommitteeAssignmentResponse = new MyCommitteeAssignmentResponse();
        myCommitteeAssignmentResponse.setProject(
                new CommitteeProjectInfosResponse()
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
                        )
        );
        myCommitteeAssignmentResponse.setAnswers(
                projectAnswers.stream()
                        .map(answer -> new CommitteeProjectQuestionResponse()
                                .id(answer.getQuestionId())
                                .question(answer.getProjectQuestion().getQuestion())
                                .required(answer.getProjectQuestion().getRequired())
                                .answer(answer.getAnswer())
                        ).toList()
        );
        myCommitteeAssignmentResponse.setVotes(votes.stream()
                .map(vote -> new CommitteeJuryVoteResponse()
                        .vote(vote.getScore())
                        .criteria(vote.getCriteria())
                        .criteriaId(vote.getCriteriaId())
                ).toList()
        );
        myCommitteeAssignmentResponse.setScore(CommitteeMapper.averageScoreOf(votes).orElse(null));
        return ResponseEntity.ok(myCommitteeAssignmentResponse);
    }
}
