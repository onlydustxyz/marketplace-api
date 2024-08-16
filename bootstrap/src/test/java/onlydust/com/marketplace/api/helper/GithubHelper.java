package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GithubHelper {

    protected static final Faker faker = new Faker();

    @Autowired
    DatabaseHelper databaseHelper;

    public GithubAccount createAccount() {
        final var login = faker.name().username();
        final var account = GithubAccount.builder()
                .id(faker.random().nextLong())
                .login(login)
                .avatarUrl(faker.internet().avatar())
                .htmlUrl("https://github.com/" + login)
                .build();

        databaseHelper.executeQuery("""
                insert into indexer_exp.github_accounts(id, login, type, html_url, avatar_url, name, bio, location, website, twitter, linkedin, telegram)
                values(:id, :login, 'USER', :htmlUrl, :avatarUrl, null, null, null, null, null, null, null);
                """, Map.of(
                "id", account.getId(),
                "login", account.getLogin(),
                "htmlUrl", account.getHtmlUrl(),
                "avatarUrl", account.getAvatarUrl()
        ));

        return account;
    }

    public GithubRepo createRepo() {
        final var owner = createAccount();

        final var name = faker.rickAndMorty().character();
        final var repo = GithubRepo.builder()
                .id(faker.random().nextLong())
                .owner(owner.getLogin())
                .name(name)
                .description(faker.lorem().paragraph())
                .forksCount(faker.random().nextLong(100))
                .starsCount(faker.random().nextLong(100))
                .htmlUrl("https://github.com/%s/%s".formatted(owner.getLogin(), name))
                .build();

        databaseHelper.executeQuery("""
                insert into indexer_exp.github_repos(id, owner_id, name, html_url, updated_at, description, stars_count, forks_count, has_issues, parent_id, owner_login, visibility)
                values(:id, :ownerId, :name, :htmlUrl, now(), :description, :starsCount, :forksCount, false, null, :ownerLogin, 'PUBLIC');
                """, Map.of(
                "id", repo.getId(),
                "ownerId", owner.getId(),
                "name", repo.getName(),
                "htmlUrl", repo.getHtmlUrl(),
                "description", repo.getDescription(),
                "starsCount", repo.getStarsCount(),
                "forksCount", repo.getForksCount(),
                "ownerLogin", owner.getLogin()
        ));

        return repo;
    }

    public void createPullRequest(GithubRepo repo, UserAuthHelper.AuthenticatedUser contributor) {
        final var prNumber = faker.random().nextInt(1000);
        final var parameters = new HashMap<String, Object>();
        parameters.put("id", faker.random().hex());
        parameters.put("repoId", repo.getId());
        parameters.put("repoOwnerLogin", repo.getOwner());
        parameters.put("repoName", repo.getName());
        parameters.put("repoHtmlUrl", repo.getHtmlUrl());
        parameters.put("prId", faker.random().nextLong());
        parameters.put("createdAt", CurrentDateProvider.now());
        parameters.put("completedAt", CurrentDateProvider.now().plusDays(1));
        parameters.put("githubNumber", prNumber);
        parameters.put("githubTitle", faker.lorem().sentence());
        parameters.put("githubHtmlUrl", "https://github.com/%s/%s/pull/%d".formatted(repo.getOwner(), repo.getName(), prNumber));
        parameters.put("githubBody", faker.lorem().paragraph());
        parameters.put("githubCommentsCount", faker.random().nextInt(10));
        parameters.put("contributorId", contributor.user().getGithubUserId());
        parameters.put("contributorLogin", contributor.user().getGithubLogin());
        parameters.put("contributorHtmlUrl", "https://github.com/" + contributor.user().getGithubLogin());
        parameters.put("contributorAvatarUrl", contributor.user().getGithubAvatarUrl());

        databaseHelper.executeQuery("""
                insert into indexer_exp.github_accounts(id, login, type, html_url, avatar_url, name, bio, location, website, twitter, linkedin, telegram)
                values(:contributorId, :contributorLogin, 'USER', :contributorHtmlUrl, :contributorAvatarUrl, null, null, null, null, null, null, null)
                on conflict do nothing;
                
                insert into indexer_exp.github_pull_requests(id, repo_id, number, title, status, created_at, closed_at, merged_at, author_id, html_url, body, comments_count, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url, review_state, commit_count)
                values (:prId, :repoId, :githubNumber, :githubTitle, 'MERGED', :createdAt, :completedAt, :completedAt, :contributorId, :githubHtmlUrl, :githubBody, :githubCommentsCount, :repoOwnerLogin, :repoName, :repoHtmlUrl, :contributorLogin, :contributorHtmlUrl, :contributorAvatarUrl, 'APPROVED', 1);
                
                insert into indexer_exp.contributions(id, repo_id, contributor_id, type, status, pull_request_id, created_at, completed_at, github_number, github_status, github_title, github_html_url, github_body, github_comments_count, repo_owner_login, repo_name, repo_html_url, github_author_id, github_author_login, github_author_html_url, github_author_avatar_url, contributor_login, contributor_html_url, contributor_avatar_url, pr_review_state) 
                values (:id, :repoId, :contributorId, 'PULL_REQUEST', 'COMPLETED', :prId, :createdAt, :completedAt, :githubNumber, 'MERGED', :githubTitle, :githubHtmlUrl, :githubBody, :githubCommentsCount, :repoOwnerLogin, :repoName, :repoHtmlUrl, :contributorId, :contributorLogin, :contributorHtmlUrl, :contributorAvatarUrl, :contributorLogin, :contributorHtmlUrl, :contributorAvatarUrl, 'APPROVED') 
                """, parameters);
    }

}
