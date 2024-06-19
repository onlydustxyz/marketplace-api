package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectApplicationsApi;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageSort;
import onlydust.com.marketplace.bff.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.bff.read.repositories.ApplicationReadRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadProjectApplicationsApiPostgresAdapter implements ReadProjectApplicationsApi {

    private final ApplicationReadRepository applicationReadRepository;

    @Override
    public ResponseEntity<ProjectApplicationPageResponse> getProjectsApplications(UUID projectId,
                                                                                  Long issueId,
                                                                                  Long applicantId,
                                                                                  Boolean isApplicantProjectMember,
                                                                                  String applicantLoginSearch,
                                                                                  ProjectApplicationPageSort sort,
                                                                                  Integer pageIndex,
                                                                                  Integer pageSize) {
        if (projectId == null && applicantId == null) {
            throw OnlyDustException.badRequest("At least one of projectId and applicantId must be provided");
        }

        final var page = applicationReadRepository.findAll(projectId,
                issueId,
                applicantId,
                isApplicantProjectMember,
                applicantLoginSearch,
                PageRequest.of(pageIndex, pageSize, Sort.by("applicant.login")));

        return ok(new ProjectApplicationPageResponse()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                .hasMore(page.hasNext())
                .applications(page.getContent().stream().map(ApplicationReadEntity::toPageItemDto).toList())
        );
    }
}
