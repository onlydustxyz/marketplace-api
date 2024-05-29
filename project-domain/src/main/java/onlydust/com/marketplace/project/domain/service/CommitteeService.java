package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeView;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class CommitteeService implements CommitteeFacadePort {

    private final CommitteeStoragePort committeeStoragePort;
    private final PermissionService permissionService;
    private final ProjectStoragePort projectStoragePort;
    private final CommitteeObserverPort committeeObserverPort;

    @Override
    public Committee createCommittee(@NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        return committeeStoragePort.save(Committee.create(name, startDate, endDate));
    }

    @Override
    public Page<CommitteeLinkView> getCommittees(Integer pageIndex, Integer pageSize) {
        return committeeStoragePort.findAll(pageIndex, pageSize);
    }

    @Override
    @Transactional
    public void update(Committee committee) {
        final var existingCommittee = committeeStoragePort.findById(committee.id())
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committee.id().value().toString())));

        // TODO remove status from update request
        if (existingCommittee.status() != committee.status())
            throw forbidden("Status cannot be updated");

        if (existingCommittee.areProjectQuestionsFixed() && !committee.projectQuestions().equals(existingCommittee.projectQuestions()))
            throw forbidden("Project questions cannot be updated");

        if (existingCommittee.areJuryCriteriaFixed() && !committee.juryCriteria().equals(existingCommittee.juryCriteria()))
            throw forbidden("Jury criteria cannot be updated");

        if (existingCommittee.areJuriesFixed() && !committee.juryIds().equals(existingCommittee.juryIds()))
            throw forbidden("Juries cannot be updated");

        committeeStoragePort.save(existingCommittee.toBuilder()
                .name(committee.name())
                .applicationStartDate(committee.applicationStartDate())
                .applicationEndDate(committee.applicationEndDate())
                .sponsorId(committee.sponsorId())
                .votePerJury(committee.votePerJury())
                .juryIds(committee.juryIds())
                .juryCriteria(committee.juryCriteria())
                .projectQuestions(committee.projectQuestions())
                .build());
    }

    @Override
    @Transactional
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeStoragePort.updateStatus(committeeId, status);
        if (status == Committee.Status.OPEN_TO_VOTES) {
            assignProjectsToJuries(committeeId);
        }
    }

    private void assignProjectsToJuries(Committee.Id committeeId) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId.value().toString())));

        if (isNull(committee.juryIds()) || committee.juryIds().isEmpty())
            throw forbidden("Committee %s must have some juries to assign them to project".formatted(committeeId.value()));

        if (isNull(committee.projectApplications()) || committee.projectApplications().isEmpty())
            throw OnlyDustException.forbidden("Committee %s must have some project applications to assign juries to them".formatted(committeeId.value()));

        final var projectIds = committee.projectApplications().keySet().stream().collect(toList());
        final var juryIds = committee.juryIds();

        if (isNull(committee.votePerJury()))
            throw forbidden("Number of vote per jury must be filled to assign juries to projects");

        if (juryIds.isEmpty() || juryIds.size() * committee.votePerJury() < projectIds.size())
            throw forbidden("Not enough juries or vote per jury to cover all projects");

        if (committee.juryCriteria().isEmpty())
            throw forbidden("Cannot assign juries to project given empty jury criteria");

        final Map<UUID, Integer> projectVoteCount = new HashMap<>();

        final Set<JuryAssignmentBuilder> juryAssignmentBuilders = juryIds.stream().map(juryId -> new JuryAssignmentBuilder(committeeId, juryId,
                        committee.votePerJury(), projectStoragePort.getProjectLedIdsForUser(juryId),
                        projectStoragePort.getProjectContributedOnIdsForUser(juryId)))
                .collect(Collectors.toSet());

        final int maxVoteNumber = Math.round((float) (juryIds.size() * committee.votePerJury()) / projectIds.size());

        for (int i = 0; i < committee.votePerJury(); i++) {
            Collections.shuffle(projectIds);
            for (UUID projectId : projectIds) {
                if (projectVoteCount.getOrDefault(projectId, 0) <= maxVoteNumber) {
                    for (JuryAssignmentBuilder juryAssignmentBuilder : juryAssignmentBuilders) {
                        if (juryAssignmentBuilder.canAssignProject(projectId)) {
                            juryAssignmentBuilder.assignProject(projectId);
                            projectVoteCount.put(projectId, projectVoteCount.getOrDefault(projectId, 0) + 1);
                            break;
                        }
                    }
                }
            }
        }

        final Set<UUID> assignedProjectIds = juryAssignmentBuilders.stream()
                .map(JuryAssignmentBuilder::getAssignedOnProjectIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (!assignedProjectIds.containsAll(projectIds)) {
            throw internalServerError("Not enough juries or vote per jury to cover all projects given some" +
                    " juries are project lead or contributor on application project");
        }

        committeeStoragePort.saveJuryAssignments(
                juryAssignmentBuilders.stream()
                        .map(juryAssignmentBuilder -> juryAssignmentBuilder.buildForCriteria(committee.juryCriteria()))
                        .flatMap(Collection::stream)
                        .toList());
    }

    @Override
    public void createUpdateApplicationForCommittee(Committee.Id committeeId, Committee.Application application) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId.value().toString())));

        checkCommitteePermission(application, committee);
        checkApplicationPermission(application.projectId(), application.userId());

        final var hasStartedApplication = committee.projectApplications().containsKey(application.projectId());
        committee.projectApplications().put(application.projectId(), application);
        committeeStoragePort.save(committee);

        if (!hasStartedApplication)
            committeeObserverPort.onNewApplication(committeeId, application.projectId(), application.userId());
    }

    private static void checkCommitteePermission(Committee.Application application, Committee committee) {
        if (committee.status() != Committee.Status.OPEN_TO_APPLICATIONS)
            throw forbidden("Applications are not opened or are closed for committee %s".formatted(committee.id().value()));

        final var projectQuestionIds = committee.projectQuestions().stream().map(ProjectQuestion::id).toList();

        if (application.answers().stream().map(Committee.ProjectAnswer::projectQuestionId).anyMatch(id -> !projectQuestionIds.contains(id)))
            throw internalServerError("A project question is not linked to committee %s".formatted(committee.id().value()));
    }

    @Override
    public CommitteeApplicationDetailsView getCommitteeApplicationDetails(Committee.Id committeeId, UUID projectId) {
        return committeeStoragePort.findByCommitteeIdAndProjectId(committeeId, projectId)
                .orElseThrow(() -> internalServerError("Application on committee %s not found for project %s"
                        .formatted(committeeId.value(), projectId)));
    }

    @Override
    public void vote(UUID juryId, Committee.Id committeeId, UUID projectId, Map<JuryCriteria.Id, Integer> scores) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId)));
        if (committee.status() != Committee.Status.OPEN_TO_VOTES)
            throw forbidden("Votes are not opened for committee %s".formatted(committee.id()));
        if (!committee.juryIds().contains(juryId))
            throw forbidden("Jury %s is not assigned to committee %s".formatted(juryId, committee.id()));

        committeeStoragePort.saveJuryVotes(juryId, committeeId, projectId, scores);
    }

    @Override
    public void allocate(final Committee.Id committeeId,
                         final UUID currencyId,
                         final BigDecimal budget,
                         final BigDecimal minAllocation,
                         final BigDecimal maxAllocation) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId.value().toString())));

        if (committee.status() != Committee.Status.CLOSED)
            throw forbidden("Committee %s must be closed to allocate budgets".formatted(committeeId.value()));

        if (minAllocation.compareTo(maxAllocation) > 0)
            throw forbidden("Min allocation is greater than max allocation");

        final var projectScores = committeeStoragePort.findJuryAssignments(committee.id())
                .stream()
                .collect(filtering(a -> a.getScore() >= 3,
                        groupingBy(JuryAssignment::getProjectId,
                                mapping(JuryAssignment::getScore,
                                        averagingDouble(Double::doubleValue)))));

        final var totalShares = projectScores.values().stream().map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
        final var perShareAllocation = budget.divide(totalShares, 6, RoundingMode.HALF_EVEN);

        final var projectAllocations = projectScores.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                e -> perShareAllocation.multiply(BigDecimal.valueOf(e.getValue()))
        ));

        committeeStoragePort.saveAllocations(committeeId, currencyId, projectAllocations);
    }

    @Override
    public void saveAllocations(Committee.Id committeeId, UUID currencyId, Map<UUID, BigDecimal> projectAllocations) {
        committeeStoragePort.saveAllocations(committeeId, currencyId, projectAllocations);
    }

    private List<ProjectAnswerView> getCommitteeAnswersWithOnlyQuestions(CommitteeView committeeView) {
        return committeeView.projectQuestions().stream()
                .map(projectQuestion -> new ProjectAnswerView(projectQuestion.id(), projectQuestion.question(), projectQuestion.required(), null)).toList();
    }

    private void checkApplicationPermission(final UUID projectId, final UUID userId) {
        if (!permissionService.isUserProjectLead(projectId, userId))
            throw forbidden("Only project lead can send new application to committee");
        if (!projectStoragePort.exists(projectId))
            throw internalServerError("Project %s was not found".formatted(projectId));
    }

}
