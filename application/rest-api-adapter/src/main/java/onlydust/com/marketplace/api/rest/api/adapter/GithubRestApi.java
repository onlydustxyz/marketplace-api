package onlydust.com.marketplace.api.rest.api.adapter;

import com.github.javafaker.Faker;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.GithubApi;
import onlydust.com.marketplace.api.contract.model.GithubUserResponse;
import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.GithubInstallationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tags(@Tag(name = "Github"))
@AllArgsConstructor
public class GithubRestApi implements GithubApi {
    private final GithubInstallationFacadePort githubInstallationFacadePort;

    private final Faker faker = new Faker();

    @Override
    public ResponseEntity<InstallationResponse> getGithubInstallation(Long installationId) {
        return githubInstallationFacadePort.getAccountByInstallationId(installationId)
                .map(GithubInstallationMapper::mapToInstallationResponse)
                .map(ResponseEntity::ok)
                .orElseThrow();
    }

    @Override
    public ResponseEntity<List<GithubUserResponse>> searchGithubUser(String search) {
        final List<GithubUserResponse> githubUserResponses = new ArrayList<>();
        for (int i = 0; i < faker.number().numberBetween(5, 50); i++) {
            final GithubUserResponse githubUserResponse = new GithubUserResponse();
            githubUserResponse.setAvatarUrl(faker.internet().url());
            githubUserResponse.setId(faker.number().randomNumber());
            githubUserResponse.setLogin(faker.rickAndMorty().character());
            githubUserResponse.setIsRegistered(faker.random().nextBoolean());
            githubUserResponses.add(githubUserResponse);
        }
        return ResponseEntity.ok(githubUserResponses);
    }
}
