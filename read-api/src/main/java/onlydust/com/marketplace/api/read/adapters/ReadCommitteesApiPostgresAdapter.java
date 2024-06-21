package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadCommitteesApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeProjectAnswerViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeLinkViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeProjectAnswerViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectInfosViewRepository;
import onlydust.com.marketplace.api.read.entities.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.api.read.mapper.CommitteeMapper;
import onlydust.com.marketplace.api.read.mapper.ProjectMapper;
import onlydust.com.marketplace.api.read.mapper.SponsorMapper;
import onlydust.com.marketplace.api.read.repositories.CommitteeJuryVoteViewRepository;
import onlydust.com.marketplace.api.read.repositories.CommitteeReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadCommitteesApiPostgresAdapter implements ReadCommitteesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final CommitteeLinkViewRepository committeeLinkViewRepository;
    private final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;
    private final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository;
    private final CommitteeReadRepository committeeReadRepository;

    @Override
    public ResponseEntity<CommitteeResponse> getCommittee(UUID committeeId) {
        final var committee = committeeReadRepository.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s not found".formatted(committeeId)));
        if (committee.status() == Committee.Status.DRAFT) {
            throw OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.toString()));
        }

        return ResponseEntity.ok(new CommitteeResponse()
                .id(committee.id())
                .name(committee.name())
                .applicationStartDate(DateMapper.ofNullable(committee.applicationStartDate()))
                .applicationEndDate(DateMapper.ofNullable(committee.applicationEndDate()))
                .status(CommitteeStatus.valueOf(committee.status().name()))
                .sponsor(SponsorMapper.mapNullabled(committee.sponsor()))
        );
    }

    @Override
    public ResponseEntity<CommitteeApplicationResponse> getCommitteeApplication(UUID committeeId, UUID projectId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var committee = committeeReadRepository.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s not found".formatted(committeeId)));

        if (projectId == null) {
            return ResponseEntity.ok(new CommitteeApplicationResponse()
                    .status(CommitteeMapper.map(committee.status()))
                    .projectQuestions(committee.projectQuestions().stream()
                            .map(question -> new CommitteeProjectQuestionResponse()
                                    .id(question.id())
                                    .question(question.question())
                                    .required(question.required())
                            ).toList())
                    .projectInfos(null)
                    .hasStartedApplication(false)
                    .applicationStartDate(DateMapper.ofNullable(committee.applicationStartDate()))
                    .applicationEndDate(DateMapper.ofNullable(committee.applicationEndDate()))
            );
        }

        if (!permissionService.isUserProjectLead(projectId, authenticatedUser.getId()))
            throw forbidden("Only project lead can get committee application");

        final var project = projectInfosViewRepository.findByProjectId(projectId)
                .orElseThrow(() -> notFound("Project %s not found".formatted(projectId)));
        final var projectAnswers = committeeProjectAnswerViewRepository.findByCommitteeIdAndAndProjectId(committeeId, projectId);
        final Map<UUID, CommitteeProjectAnswerViewEntity> answersByQuestionId = projectAnswers.stream()
                .collect(Collectors.toMap(CommitteeProjectAnswerViewEntity::getQuestionId, a -> a));

        return ResponseEntity.ok(new CommitteeApplicationResponse()
                .status(CommitteeMapper.map(committee.status()))
                .projectQuestions(committee.projectQuestions().stream()
                        .map(q -> new CommitteeProjectQuestionResponse()
                                .id(q.id())
                                .question(q.question())
                                .answer(Optional.ofNullable(answersByQuestionId.get(q.id())).map(CommitteeProjectAnswerViewEntity::getAnswer).orElse(null))
                                .required(q.required())

                        ).toList())
                .projectInfos(CommitteeMapper.map(project))
                .hasStartedApplication(nonNull(projectAnswers) && !projectAnswers.isEmpty())
                .applicationStartDate(DateMapper.ofNullable(committee.applicationStartDate()))
                .applicationEndDate(DateMapper.ofNullable(committee.applicationEndDate()))
        );
    }

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
                                        .map(CommitteeMapper::roundScore)
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
        myCommitteeAssignmentResponse.setProject(CommitteeMapper.map(project));
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
