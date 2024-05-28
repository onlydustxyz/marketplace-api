package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeCommitteesReadApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.AllUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.bff.read.entities.CommitteeJuryVoteReadEntity;
import onlydust.com.marketplace.bff.read.entities.CommitteeProjectAnswerReadEntity;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import onlydust.com.marketplace.bff.read.mapper.SponsorMapper;
import onlydust.com.marketplace.bff.read.mapper.UserMapper;
import onlydust.com.marketplace.bff.read.repositories.CommitteeReadRepository;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeCommitteeMapper.statusToResponse;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeCommitteesReadApiPostgresAdapter implements BackofficeCommitteesReadApi {

    private final CommitteeReadRepository committeeReadRepository;

    @Override
    public ResponseEntity<CommitteeResponse> getCommittee(UUID committeeId) {
        final var committee = committeeReadRepository.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s not found".formatted(committeeId)));

        final Map<UUID, Double> averageVotePerProjects = committee.juryVotes().stream()
                .filter(v -> v.score() != null)
                .collect(groupingBy(
                        CommitteeJuryVoteReadEntity::projectId,
                        Collectors.averagingInt(CommitteeJuryVoteReadEntity::score)));

        final Map<UUID, CommitteeProjectAnswerReadEntity> mostRecentAnswerPerProject = committee.projectAnswers().stream()
                .collect(groupingBy(CommitteeProjectAnswerReadEntity::projectId,
                        Collectors.collectingAndThen(toList(), l -> l.stream()
                                .max(Comparator.comparing(CommitteeProjectAnswerReadEntity::techUpdatedAt))
                                .orElseThrow())));

        final List<ApplicationResponse> projectApplications = mostRecentAnswerPerProject.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(a -> new ApplicationResponse()
                        .project(ProjectMapper.mapBO(a.project()))
                        .applicant(UserMapper.map(a.user()))
                        .score(Optional.ofNullable(averageVotePerProjects.get(a.projectId())).map(BigDecimal::valueOf).orElse(null))
                        .allocatedBudget(null))// TODO: implement
                .toList();

        final Map<AllUserViewEntity, Map<ProjectLinkViewEntity, List<CommitteeJuryVoteReadEntity>>> votesPerUserPerProject = committee.juryVotes().stream()
                .collect(groupingBy(CommitteeJuryVoteReadEntity::user, groupingBy(CommitteeJuryVoteReadEntity::project)));

        final List<JuryAssignmentResponse> juryAssignments = new ArrayList<>();
        votesPerUserPerProject.forEach((user, votesPerProject) -> {
            juryAssignments.add(new JuryAssignmentResponse()
                    .user(UserMapper.map(user))
                    .projectsAssigned(votesPerProject.keySet().stream().map(ProjectMapper::mapBO).toList())
                    .totalAssignment(votesPerProject.size())
                    .completedAssignments((int) votesPerProject.entrySet().stream()
                            .filter(e -> e.getValue().stream().anyMatch(v -> v.score() != null)).count())
            );
        });

        final var response = new CommitteeResponse()
                .id(committee.id())
                .name(committee.name())
                .applicationStartDate(DateMapper.ofNullable(committee.applicationStartDate()))
                .applicationEndDate(DateMapper.ofNullable(committee.applicationEndDate()))
                .status(statusToResponse(committee.status()))
                .sponsor(SponsorMapper.mapNullable(committee.sponsor()))
                .votePerJury(committee.votePerJury())
                .projectCount(projectApplications.size())
                .applications(projectApplications)
                .projectQuestions(committee.projectQuestions().stream()
                        .map(q -> new ProjectQuestionResponse(q.id(), q.question(), q.required())).toList())
                .juryCriteria(committee.juryCriterias().stream()
                        .map(c -> new JuryCriteriaResponse(c.id(), c.criteria())).toList())
                .juries(committee.juries().stream()
                        .map(j -> UserMapper.map(j.user())).toList())
                .juryAssignments(juryAssignments)
                .totalAssignments(juryAssignments.stream().mapToInt(JuryAssignmentResponse::getTotalAssignment).sum())
                .completedAssignments(juryAssignments.stream().mapToInt(JuryAssignmentResponse::getCompletedAssignments).sum());

        return ResponseEntity.ok(response);
    }
}
