package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.GithubApi;
import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.GithubMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@RestController
@Tags(@Tag(name = "Github"))
@AllArgsConstructor
public class GithubRestApi implements GithubApi {
    private final GithubInstallationFacadePort githubInstallationFacadePort;

    @Override
    public ResponseEntity<InstallationResponse> getGithubInstallation(Long installationId) {
        return githubInstallationFacadePort.getAccountByInstallationId(installationId)
                .map(account -> GithubMapper.mapToInstallationResponse(installationId, account))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> OnlyDustException.notFound(format("Installation %d not found", installationId)));
    }
}
