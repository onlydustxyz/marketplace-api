package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.GithubApi;
import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.GithubInstallationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Github"))
@AllArgsConstructor
public class GithubRestApi implements GithubApi {
    private final GithubInstallationFacadePort githubInstallationFacadePort;


    @Override
    public ResponseEntity<InstallationResponse> getGithubInstallation(Long installationId) {
        return githubInstallationFacadePort.getAccountByInstallationId(installationId)
                .map(GithubInstallationMapper::mapToInstallationResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> OnlydustException.builder().status(HttpStatus.NOT_FOUND.value()).message("Installation not found").build());
    }
}
