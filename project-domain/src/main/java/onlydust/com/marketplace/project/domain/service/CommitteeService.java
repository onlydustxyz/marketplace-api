package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CommitteeService implements CommitteeFacadePort {

    private final CommitteeStoragePort committeeStoragePort;
    private final PermissionService permissionService;
    private final ProjectStoragePort projectStoragePort;

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
        checkCommitteePermission(committeeId);
        checkApplicationPermission(application.projectId(), application.userId());
        committeeStoragePort.saveApplication(committeeId, application);
    }

    @Override
    public CommitteeApplicationView getCommitteeApplication(Committee.Id committeeId, Optional<UUID> optionalProjectId, UUID userId) {
        final CommitteeView committeeView = checkCommitteePermission(committeeId);
        if (optionalProjectId.isPresent()) {
            final UUID projectId = optionalProjectId.get();
            checkApplicationPermission(projectId, userId);
            List<ProjectAnswerView> projectAnswers = committeeStoragePort.getApplicationAnswers(committeeId, projectId);
            if (isNull(projectAnswers) || projectAnswers.isEmpty()) {
                projectAnswers = getCommitteeAnswersWithOnlyQuestions(committeeView);

            }
            return new CommitteeApplicationView(committeeView.status(), projectAnswers, projectStoragePort.getProjectInfos(projectId));
        } else {
            return new CommitteeApplicationView(committeeView.status(), getCommitteeAnswersWithOnlyQuestions(committeeView), null);
        }
    }


    private List<ProjectAnswerView> getCommitteeAnswersWithOnlyQuestions(CommitteeView committeeView) {
        return committeeView.projectQuestions().stream()
                .map(projectQuestion -> new ProjectAnswerView(projectQuestion.id(), projectQuestion.question(), projectQuestion.required(), null)).toList();
    }

    private void checkApplicationPermission(final UUID projectId, final UUID userId) {
        if (!permissionService.isUserProjectLead(projectId, userId)) throw OnlyDustException.forbidden("Only project leads send new application to committee");
        if (!projectStoragePort.exists(projectId)) throw OnlyDustException.internalServerError("Project %s was not found".formatted(projectId));
    }

    private CommitteeView checkCommitteePermission(final Committee.Id committeeId) {
        final CommitteeView committeeView =
                committeeStoragePort.findById(committeeId).orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));
        if (committeeView.status() != Committee.Status.OPEN_TO_APPLICATIONS)
            throw OnlyDustException.forbidden("Applications are not opened or are closed for committee %s".formatted(committeeId.value()));
        return committeeView;
    }
}
