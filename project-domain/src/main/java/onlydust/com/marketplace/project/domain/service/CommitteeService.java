package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

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
        committeeStoragePort.save(committee);
        if (committee.status() == Committee.Status.DRAFT && !committee.projectQuestions().isEmpty()) {
            committeeStoragePort.deleteAllProjectQuestions(committee.id());
            committeeStoragePort.saveProjectQuestions(committee.id(), committee.projectQuestions());
        }
    }

    @Override
    public CommitteeView getCommitteeById(Committee.Id committeeId) {
        return committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));
    }

    @Override
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeStoragePort.updateStatus(committeeId, status);
    }

    @Override
    public void createUpdateApplicationForCommittee(Committee.Id committeeId, Committee.Application application) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));
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
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));

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
