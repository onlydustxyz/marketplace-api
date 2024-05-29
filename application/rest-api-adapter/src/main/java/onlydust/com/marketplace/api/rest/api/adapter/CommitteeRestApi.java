package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.CommitteesApi;
import onlydust.com.marketplace.api.contract.model.CommitteeApplicationRequest;
import onlydust.com.marketplace.api.contract.model.CommitteeApplicationResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.CommitteeMapper;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.CommitteeMapper.committeeApplicationViewToResponse;

@RestController
@Tags(@Tag(name = "Committees"))
@AllArgsConstructor
public class CommitteeRestApi implements CommitteesApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final CommitteeFacadePort committeeFacadePort;

    @Override
    public ResponseEntity<CommitteeApplicationResponse> getApplication(UUID committeeId, UUID projectId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final CommitteeApplicationView committeeApplicationView = committeeFacadePort.getCommitteeApplication(Committee.Id.of(committeeId), Optional.ofNullable(projectId),
                authenticatedUser.getId());
        return ResponseEntity.ok(committeeApplicationViewToResponse(committeeApplicationView));
    }

    @Override
    public ResponseEntity<Void> createUpdateApplicationForCommittee(UUID committeeId, UUID projectId, CommitteeApplicationRequest committeeApplicationRequest) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Committee.Application application = CommitteeMapper.committeeApplicationRequestToDomain(committeeApplicationRequest, authenticatedUser.getId(),
                projectId);
        committeeFacadePort.createUpdateApplicationForCommittee(Committee.Id.of(committeeId), application);
        return ResponseEntity.noContent().build();
    }
}
