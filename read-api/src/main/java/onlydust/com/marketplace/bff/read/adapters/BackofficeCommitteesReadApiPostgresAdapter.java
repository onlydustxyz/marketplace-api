package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeCommitteesReadApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeProjectAnswerViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectInfosViewRepository;
import onlydust.com.marketplace.bff.read.entities.ShortCurrencyResponseEntity;
import onlydust.com.marketplace.bff.read.entities.committee.CommitteeBudgetAllocationReadEntity;
import onlydust.com.marketplace.bff.read.entities.committee.CommitteeJuryVoteReadEntity;
import onlydust.com.marketplace.bff.read.entities.committee.CommitteeProjectAnswerReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.bff.read.mapper.CommitteeMapper;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import onlydust.com.marketplace.bff.read.mapper.SponsorMapper;
import onlydust.com.marketplace.bff.read.repositories.CommitteeBudgetAllocationsResponseEntityRepository;
import onlydust.com.marketplace.bff.read.repositories.CommitteeReadRepository;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeCommitteeMapper.statusToResponse;
import static onlydust.com.marketplace.bff.read.mapper.CommitteeMapper.roundScore;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeCommitteesReadApiPostgresAdapter implements BackofficeCommitteesReadApi {

    private final CommitteeReadRepository committeeReadRepository;
    private final CommitteeBudgetAllocationsResponseEntityRepository committeeBudgetAllocationsResponseEntityRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;
    private final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository;

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

        final Map<UUID, MoneyResponse> projectAllocations = committee.budgetAllocations().stream()
                .collect(toMap(CommitteeBudgetAllocationReadEntity::projectId, a -> new MoneyResponse(a.amount(), a.currency().toDto())));

        final List<ApplicationResponse> projectApplications = mostRecentAnswerPerProject.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(a -> new ApplicationResponse()
                        .project(ProjectMapper.mapBO(a.project()))
                        .applicant(a.user().toLinkResponse())
                        .score(Optional.ofNullable(averageVotePerProjects.get(a.projectId())).map(BigDecimal::valueOf).map(CommitteeMapper::roundScore).orElse(null))
                        .allocation(projectAllocations.get(a.projectId())))
                .toList();

        final Map<AllUserReadEntity, Map<ProjectLinkViewEntity, List<CommitteeJuryVoteReadEntity>>> votesPerUserPerProject = committee.juryVotes().stream()
                .collect(groupingBy(CommitteeJuryVoteReadEntity::user, groupingBy(CommitteeJuryVoteReadEntity::project)));

        final List<JuryAssignmentResponse> juryAssignments = new ArrayList<>();
        votesPerUserPerProject.forEach((user, votesPerProject) -> {
            juryAssignments.add(new JuryAssignmentResponse()
                    .user(user.toLinkResponse())
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
                .sponsor(SponsorMapper.mapNullableBO(committee.sponsor()))
                .votePerJury(committee.votePerJury())
                .juryCount(isNull(committee.juries()) ? null : committee.juries().size())
                .projectCount(projectApplications.size())
                .applications(projectApplications)
                .projectQuestions(committee.projectQuestions().stream()
                        .map(q -> new ProjectQuestionResponse(q.id(), q.question(), q.required())).toList())
                .juryCriteria(committee.juryCriterias().stream()
                        .map(c -> new JuryCriteriaResponse(c.id(), c.criteria())).toList())
                .juries(committee.juries().stream()
                        .map(j -> j.user().toLinkResponse()).toList())
                .juryAssignments(juryAssignments)
                .totalAssignments(juryAssignments.stream().mapToInt(JuryAssignmentResponse::getTotalAssignment).sum())
                .completedAssignments(juryAssignments.stream().mapToInt(JuryAssignmentResponse::getCompletedAssignments).sum())
                .allocation(projectAllocations.values().stream()
                        .reduce((left, right) -> new MoneyResponse(left.getAmount().add(right.getAmount()), left.getCurrency()))
                        .orElse(null));

        return ok(response);
    }

    @Override
    public ResponseEntity<CommitteeProjectApplicationResponse> getProjectApplication(UUID committeeId, UUID projectId) {
        final var committee = committeeReadRepository.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s not found".formatted(committeeId)));

        final var project = projectInfosViewRepository.findByProjectId(projectId)
                .orElseThrow(() -> notFound("Project %s not found".formatted(projectId)));

        final var projectAnswers = committeeProjectAnswerViewRepository.findByCommitteeIdAndAndProjectId(committeeId, projectId);

        final List<JuryVoteResponse> juryVotes = committee.juryVotes().stream()
                .filter(v -> v.projectId().equals(projectId))
                .collect(groupingBy(CommitteeJuryVoteReadEntity::user, toList()))
                .entrySet().stream()
                .map(e -> new JuryVoteResponse()
                        .totalScore(roundScore(e.getValue().stream()
                                .filter(v -> v.score() != null)
                                .mapToInt(CommitteeJuryVoteReadEntity::score)
                                .average()))
                        .jury(e.getKey().toLinkResponse())
                        .answers(e.getValue().stream()
                                .map(a -> new ScoredAnswerResponse()
                                        .criteria(a.criteria().criteria())
                                        .score(a.score()))
                                .toList()))
                .toList();

        final var totalScore = juryVotes.stream()
                .map(v -> v.getTotalScore())
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average();

        final var allocation = committee.budgetAllocations().stream()
                .filter(a -> a.projectId().equals(projectId))
                .map(a -> new MoneyResponse(a.amount(), a.currency().toDto()))
                .reduce((left, right) -> new MoneyResponse(left.getAmount().add(right.getAmount()), left.getCurrency()))
                .orElse(null);

        final var response = new CommitteeProjectApplicationResponse()
                .project(new ProjectLinkResponse()
                        .id(project.id())
                        .slug(project.slug())
                        .name(project.name())
                        .logoUrl(isNull(project.logoUrl()) ? null : project.logoUrl())
                )
                .projectQuestions(projectAnswers.stream()
                        .map(answer -> new ProjectAnswerResponse()
                                .answer(answer.getAnswer())
                                .question(answer.getProjectQuestion().getQuestion())
                                .required(answer.getProjectQuestion().getRequired())
                        )
                        .toList()
                )
                .totalScore(totalScore == null ? null : roundScore(totalScore))
                .juryVotes(juryVotes)
                .allocation(allocation);

        return ok(response);
    }

    @Override
    public ResponseEntity<CommitteeBudgetAllocationsResponse> getBudgetAllocations(UUID committeeId) {
        final var committeeBudgetAllocations = committeeBudgetAllocationsResponseEntityRepository.findAllByCommitteeId(committeeId);
        return ok(new CommitteeBudgetAllocationsResponse()
                .totalAllocationAmount(committeeBudgetAllocations.stream()
                        .map(CommitteeBudgetAllocationReadEntity::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .currency(committeeBudgetAllocations.stream().findFirst()
                        .map(CommitteeBudgetAllocationReadEntity::currency).map(ShortCurrencyResponseEntity::toDto)
                        .orElse(null))
                .projectAllocations(committeeBudgetAllocations.stream()
                        .map(CommitteeBudgetAllocationReadEntity::toDto)
                        .toList()));
    }
}
