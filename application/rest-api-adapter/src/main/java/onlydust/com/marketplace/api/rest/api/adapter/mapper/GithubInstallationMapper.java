package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.GithubOrganizationResponse;
import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.api.contract.model.ShortGithubRepoResponse;
import onlydust.com.marketplace.api.domain.model.GithubAccount;

import java.util.Comparator;

public class GithubInstallationMapper {
    public static InstallationResponse mapToInstallationResponse(Long installationId, GithubAccount githubAccount) {
        var organization = new GithubOrganizationResponse();
        organization.setId(githubAccount.id());
        organization.setLogin(githubAccount.login());
        organization.setAvatarUrl(githubAccount.avatarUrl());
        organization.setHtmlUrl(githubAccount.htmlUrl());
        organization.setName(githubAccount.name());
        organization.setRepos(
                githubAccount.repos().stream()
                        .map(repo -> {
                            var installedRepo = new ShortGithubRepoResponse();
                            installedRepo.setId(repo.getId());
                            installedRepo.setName(repo.getName());
                            installedRepo.setOwner(repo.getOwner());
                            installedRepo.setHtmlUrl(repo.getHtmlUrl());
                            installedRepo.setDescription(repo.getDescription());
                            return installedRepo;
                        })
                        .sorted(Comparator.comparing(ShortGithubRepoResponse::getId))
                        .toList());

        var installation = new InstallationResponse();
        installation.setId(installationId);
        installation.setOrganization(organization);
        return installation;
    }
}
