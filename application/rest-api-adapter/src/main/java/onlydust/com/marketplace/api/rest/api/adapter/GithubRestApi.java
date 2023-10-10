package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.GithubApi;
import onlydust.com.marketplace.api.contract.model.GithubUserResponse;
import onlydust.com.marketplace.api.contract.model.InstalledGithubRepoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tags(@Tag(name = "Github"))
@AllArgsConstructor
public class GithubRestApi implements GithubApi {

    @Override
    public ResponseEntity<List<InstalledGithubRepoResponse>> getGithubRepositoriesForInstallationId(Long installationId) {
        return GithubApi.super.getGithubRepositoriesForInstallationId(installationId);
    }

    @Override
    public ResponseEntity<List<GithubUserResponse>> searchGithubUser(String search) {
        return GithubApi.super.searchGithubUser(search);
    }
}
