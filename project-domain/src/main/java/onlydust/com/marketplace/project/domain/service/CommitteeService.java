package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignmentBuilder;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class CommitteeService implements CommitteeFacadePort {

    private final CommitteeStoragePort committeeStoragePort;
    private final PermissionService permissionService;
    private final ProjectStoragePort projectStoragePort;
    private final CommitteeObserverPort committeeObserverPort;

    @Override
    public Committee createCommittee(@NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        return committeeStoragePort.save(new Committee(name, startDate, endDate));
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

        if (existingCommittee.status() != Committee.Status.DRAFT && !committee.projectQuestions().equals(existingCommittee.projectQuestions()))
            throw forbidden("Project questions can only be updated for draft committees");

        committeeStoragePort.save(committee);

        if (List.of(Committee.Status.DRAFT, Committee.Status.OPEN_TO_APPLICATIONS).contains(committee.status())) {
            committeeStoragePort.deleteAllJuries(committee.id());
            if (!committee.juryIds().isEmpty()) {
                committeeStoragePort.saveJuries(committee.id(), committee.juryIds());
            }
            committeeStoragePort.deleteAllJuryCriteria(committee.id());
            if (!committee.juryCriteria().isEmpty()) {
                committeeStoragePort.saveJuryCriteria(committee.id(), committee.juryCriteria());
            }
        }
    }

    @Override
    public CommitteeView getCommitteeById(Committee.Id committeeId) {
        return committeeStoragePort.findViewById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId.value().toString())));
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
        final CommitteeView committee = getCommitteeById(committeeId);
        if (isNull(committee.juries()) || committee.juries().isEmpty()) {
            throw OnlyDustException.forbidden("Committee %s must have some juries to assign them to project".formatted(committeeId.value()));
        }
        List<UUID> projectIds = committee.committeeApplicationLinks().stream()
                .map(committeeApplicationLinkView -> committeeApplicationLinkView.projectShortView().id()).collect(Collectors.toList());
        final Set<UUID> juryIds = committee.juries().stream().map(RegisteredContributorLinkView::getId).collect(Collectors.toSet());

        if (isNull(committee.votePerJury())) {
            throw OnlyDustException.forbidden("Number of vote per jury must filled to assign juries to projects");
        }
        if (juryIds.isEmpty() || juryIds.size() * committee.votePerJury() < projectIds.size()) {
            throw OnlyDustException.forbidden("Not enough juries or vote per jury to cover all projects");
        }
        if (committee.juryCriteria().isEmpty()) {
            throw OnlyDustException.forbidden("Cannot assign juries to project given empty jury criteria");
        }

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
            throw OnlyDustException.internalServerError("Not enough juries or vote per jury to cover all projects given some" +
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
        final var committee = committeeStoragePort.findViewById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId.value().toString())));
        checkCommitteePermission(application, committee);
        checkApplicationPermission(application.projectId(), application.userId());
        final boolean hasStartedApplication = committeeStoragePort.hasStartedApplication(committeeId, application);
        committeeStoragePort.saveApplication(committeeId, application);
        if (!hasStartedApplication) {
            committeeObserverPort.onNewApplication(committeeId, application.projectId(), application.userId());
        }
    }

    private static void checkCommitteePermission(Committee.Application application, CommitteeView committee) {
        if (committee.status() != Committee.Status.OPEN_TO_APPLICATIONS)
            throw OnlyDustException.forbidden("Applications are not opened or are closed for committee %s".formatted(committee.id().value()));
        final List<ProjectQuestion.Id> projectQuestionIds = committee.projectQuestions().stream().map(ProjectQuestion::id).toList();
        if (application.answers().stream().map(Committee.ProjectAnswer::projectQuestionId)
                .anyMatch(id -> !projectQuestionIds.contains(id))) {
            throw OnlyDustException.internalServerError("A project question is not linked to committee %s".formatted(committee.id().value()));
        }
    }

    @Override
    public CommitteeApplicationView getCommitteeApplication(Committee.Id committeeId, Optional<UUID> optionalProjectId, UUID userId) {
        final var committee = committeeStoragePort.findViewById(committeeId)
                .orElseThrow(() -> notFound("Committee %s was not found".formatted(committeeId.value().toString())));

        if (optionalProjectId.isPresent()) {
            final UUID projectId = optionalProjectId.get();
            checkApplicationPermission(projectId, userId);
            List<ProjectAnswerView> projectAnswers = committeeStoragePort.getApplicationAnswers(committeeId, projectId);
            Boolean hasStartedApplication = nonNull(projectAnswers) && !projectAnswers.isEmpty();
            if (!hasStartedApplication) {
                projectAnswers = getCommitteeAnswersWithOnlyQuestions(committee);
            }
            return new CommitteeApplicationView(committee.status(), projectAnswers, projectStoragePort.getProjectInfos(projectId), hasStartedApplication,
                    committee.applicationStartDate(), committee.applicationEndDate());
        }

        return new CommitteeApplicationView(committee.status(), getCommitteeAnswersWithOnlyQuestions(committee), null, false,
                committee.applicationStartDate(), committee.applicationEndDate());
    }

    @Override
    public CommitteeApplicationDetailsView getCommitteeApplicationDetails(Committee.Id committeeId, UUID projectId) {
        return committeeStoragePort.findByCommitteeIdAndProjectId(committeeId, projectId)
                .orElseThrow(() -> OnlyDustException.internalServerError("Application on committee %s not found for project %s"
                        .formatted(committeeId.value(), projectId)));
    }

    @Override
    public CommitteeJuryVotesView getCommitteeJuryVotesForProject(UUID userId, Committee.Id committeeId, UUID projectId) {
        return new CommitteeJuryVotesView(Committee.Status.OPEN_TO_VOTES, List.of(), projectStoragePort.getProjectInfos(projectId), false);
    }

    private List<ProjectAnswerView> getCommitteeAnswersWithOnlyQuestions(CommitteeView committeeView) {
        return committeeView.projectQuestions().stream()
                .map(projectQuestion -> new ProjectAnswerView(projectQuestion.id(), projectQuestion.question(), projectQuestion.required(), null)).toList();
    }

    private void checkApplicationPermission(final UUID projectId, final UUID userId) {
        if (!permissionService.isUserProjectLead(projectId, userId))
            throw OnlyDustException.forbidden("Only project lead can send new application to committee");
        if (!projectStoragePort.exists(projectId))
            throw OnlyDustException.internalServerError("Project %s was not found".formatted(projectId));
    }

}
