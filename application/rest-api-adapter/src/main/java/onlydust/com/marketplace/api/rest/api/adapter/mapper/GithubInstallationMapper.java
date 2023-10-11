package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.InstallationResponse;
import onlydust.com.marketplace.api.contract.model.InstalledGithubOrganizationResponse;
import onlydust.com.marketplace.api.contract.model.InstalledGithubRepoResponse;
import onlydust.com.marketplace.api.domain.model.GithubAccount;

public class GithubInstallationMapper {
    public static InstallationResponse mapToInstallationResponse(GithubAccount githubAccount) {
        var organization = new InstalledGithubOrganizationResponse();
        organization.setName(githubAccount.login());
        organization.setLogoUrl(githubAccount.avatarUrl());

        var installation = new InstallationResponse();
        installation.setOrganization(organization);
        githubAccount.repos().forEach(repo -> {
            var installedGithubRepoResponse = new InstalledGithubRepoResponse();
            installedGithubRepoResponse.setName(repo.name());
            installedGithubRepoResponse.setShortDescription(repo.description());
            installedGithubRepoResponse.setGithubId(repo.id());
            installation.addReposItem(installedGithubRepoResponse);
        });

        return installation;
    }
}
