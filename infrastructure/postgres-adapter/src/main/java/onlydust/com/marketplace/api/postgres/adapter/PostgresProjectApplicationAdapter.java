package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class PostgresProjectApplicationAdapter implements ProjectApplicationStoragePort {

    private final ApplicationRepository applicationRepository;

    @Override
    public boolean saveNew(@NonNull Application application) {
        try {
            applicationRepository.saveAndFlush(ApplicationEntity.fromDomain(application));
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn("Application %s already exists".formatted(application), e);
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void save(@NonNull Application... applications) {
        applicationRepository.saveAll(Arrays.stream(applications).map(ApplicationEntity::fromDomain).toList());
    }

    @Override
    public Optional<Application> findApplication(Long applicantId, ProjectId projectId, GithubIssue.Id issueId) {
        return applicationRepository.findByApplicantIdAndProjectIdAndIssueId(applicantId, projectId.value(), issueId.value())
                .map(ApplicationEntity::toDomain);
    }

    @Override
    public List<Application> findApplications(Long applicantId, GithubIssue.Id issueId) {
        return applicationRepository.findAllByApplicantIdAndIssueId(applicantId, issueId.value()).stream()
                .map(ApplicationEntity::toDomain).toList();
    }

    @Override
    public List<Application> findApplications(GithubComment.Id commentId) {
        return applicationRepository.findAllByCommentId(commentId.value()).stream()
                .map(ApplicationEntity::toDomain).toList();
    }

    @Override
    public List<Application> findApplications(GithubIssue.Id issueId) {
        return applicationRepository.findAllByIssueId(issueId.value()).stream()
                .map(ApplicationEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteApplications(Application.Id... applicationIds) {
        applicationRepository.deleteAllById(Arrays.stream(applicationIds).map(UuidWrapper::value).toList());
    }

    @Override
    @Transactional
    public void deleteApplicationsByIssueId(GithubIssue.Id issueId) {
        applicationRepository.deleteAllByIssueId(issueId.value());
    }

    @Override
    public Optional<Application> findApplication(Application.Id id) {
        return applicationRepository.findById(id.value())
                .map(ApplicationEntity::toDomain);
    }

    @Override
    @Transactional
    public void deleteObsoleteApplications() {
        applicationRepository.deleteObsoleteApplications();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> findApplicationsOnIssueAndProject(GithubIssue.Id issueId, ProjectId projectId) {
        return applicationRepository.findAllByProjectIdAndIssueId(projectId.value(), issueId.value())
                .stream()
                .map(ApplicationEntity::toDomain)
                .toList();
    }
}
