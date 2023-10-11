package onlydust.com.marketplace.api.rest.api.adapter;

import com.github.javafaker.Faker;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.GithubApi;
import onlydust.com.marketplace.api.contract.model.GithubUserResponse;
import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.api.contract.model.InstalledGithubOrganizationResponse;
import onlydust.com.marketplace.api.contract.model.InstalledGithubRepoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tags(@Tag(name = "Github"))
@AllArgsConstructor
public class GithubRestApi implements GithubApi {

    private final Faker faker = new Faker();

    @Override
    public ResponseEntity<InstallationResponse> getGithubInstallation(Long installationId) {
        final InstallationResponse installationResponse = new InstallationResponse();
        final InstalledGithubOrganizationResponse organization = new InstalledGithubOrganizationResponse();
        organization.setName(faker.pokemon().name());
        organization.setLogoUrl(faker.internet().url());
        installationResponse.setOrganization(organization);
        for (int i = 0; i < 5; i++) {
            final InstalledGithubRepoResponse reposItem = new InstalledGithubRepoResponse();
            reposItem.setName(faker.harryPotter().character());
            reposItem.setGithubId(faker.number().randomNumber());
            reposItem.setShortDescription(faker.rickAndMorty().location());
            installationResponse.addReposItem(reposItem);
        }
        return ResponseEntity.ok(installationResponse);
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
