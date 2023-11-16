package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.GithubApi;
import onlydust.com.marketplace.api.contract.model.GithubOrganizationResponse;
import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.api.contract.model.ShortGithubRepoResponse;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.GithubInstallationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.lang.String.format;

@RestController
@Tags(@Tag(name = "Github"))
@AllArgsConstructor
public class GithubRestApi implements GithubApi {
    private final GithubInstallationFacadePort githubInstallationFacadePort;


    @Override
    public ResponseEntity<InstallationResponse> getGithubInstallation(Long installationId) {
        return githubInstallationFacadePort.getAccountByInstallationId(installationId)
                .map(account -> GithubInstallationMapper.mapToInstallationResponse(installationId, account))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> OnlyDustException.notFound(format("Installation %d not found", installationId)));
    }

    @Override
    public ResponseEntity<List<GithubOrganizationResponse>> searchGithubUserOrganizations(Long githubUserId) {
        return ResponseEntity.ok(List.of(
                new GithubOrganizationResponse()
                        .name("Symeo.io")
                        .id(105865802L)
                        .avatarUrl("https://avatars.githubusercontent.com/u/105865802?v=4")
                        .htmlUrl("https://github.com/symeo-io")
                        .login("symeo-io")
                        .installed(true)
                        .repos(List.of(
                                new ShortGithubRepoResponse()
                                        .name("symeo-monolithic-backend")
                                        .id(495382833L)
                                        .htmlUrl("https://github.com/symeo-io/symeo-monolithic-backend")
                                        .description(null)
                        )),
                new GithubOrganizationResponse()
                        .name("Hasura")
                        .id(13966722L)
                        .avatarUrl("https://avatars.githubusercontent.com/u/13966722?v=4")
                        .htmlUrl("https://github.com/hasura")
                        .login("hasura")
                        .installed(false)
                        .repos(List.of(
                                new ShortGithubRepoResponse()
                                        .name("graphql-engine")
                                        .id(137724480L)
                                        .htmlUrl("https://github.com/hasura/graphql-engine")
                                        .description("Blazing fast, instant realtime GraphQL APIs on your DB with " +
                                                     "fine grained access control, also trigger webhooks on database " +
                                                     "events.")
                        ))
        ));
    }
}
