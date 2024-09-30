package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubHelper {

    protected static final Faker faker = new Faker();

    @Autowired
    ProjectHelper projectHelper;
    @Autowired
    DatabaseHelper databaseHelper;

    public GithubAccount createAccount() {
        return createAccount(faker.random().nextLong());
    }

    public GithubAccount createAccount(Long id) {
        return createAccount(id, faker.name().username(), faker.internet().avatar());
    }

    public GithubAccount createAccount(UserAuthHelper.AuthenticatedUser contributor) {
        return createAccount(contributor.user().getGithubUserId(), contributor.user().getGithubLogin(), contributor.user().getGithubAvatarUrl());
    }

    public GithubAccount createAccount(Long id, String login, String avatarUrl) {
        final var account = GithubAccount.builder()
                .id(id)
                .login(login)
                .avatarUrl(avatarUrl)
                .htmlUrl("https://github.com/" + login)
                .build();

        databaseHelper.executeQuery("""
                insert into indexer_exp.github_accounts(id, login, type, html_url, avatar_url, name, bio, location, website, twitter, linkedin, telegram)
                values(:id, :login, 'USER', :htmlUrl, :avatarUrl, null, null, null, null, null, null, null)
                on conflict do nothing;
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

    public GithubRepo createRepo(final @NonNull ProjectId projectId) {
        final var repo = createRepo();
        projectHelper.addRepo(projectId, repo.getId());
        return repo;
    }

    public void createPullRequest(GithubRepo repo, UserAuthHelper.AuthenticatedUser contributor) {
        createPullRequest(repo, contributor, null);
    }

    public long createPullRequest(GithubRepo repo, UserAuthHelper.AuthenticatedUser contributor, List<String> mainFileExtensions) {
        final var prId = faker.random().nextLong();
        final var prNumber = faker.random().nextInt(1000);

        createAccount(contributor);

        final var parameters = new HashMap<String, Object>();
        parameters.put("prId", prId);
        parameters.put("repoId", repo.getId());
        parameters.put("contributorId", contributor.user().getGithubUserId());
        parameters.put("createdAt", CurrentDateProvider.now());
        parameters.put("completedAt", CurrentDateProvider.now().plusDays(1));
        parameters.put("githubNumber", prNumber);
        parameters.put("githubTitle", faker.lorem().sentence());
        parameters.put("githubHtmlUrl", "https://github.com/%s/%s/pull/%d".formatted(repo.getOwner(), repo.getName(), prNumber));
        parameters.put("githubBody", faker.lorem().paragraph());
        parameters.put("githubCommentsCount", faker.random().nextInt(10));
        parameters.put("mainFileExtensions", mainFileExtensions == null ? new String[]{} : mainFileExtensions.toArray(String[]::new));

        databaseHelper.executeQuery("""
                insert into indexer_exp.github_pull_requests(id, repo_id, number, title, status, created_at, closed_at, merged_at, author_id, html_url, body, comments_count, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url, review_state, commit_count, main_file_extensions)
                select :prId,
                       gr.id,
                       :githubNumber,
                       :githubTitle,
                       'MERGED',
                       :createdAt,
                       :completedAt,
                       :completedAt,
                       ga.id,
                       :githubHtmlUrl,
                       :githubBody,
                       :githubCommentsCount,
                       gr.owner_login,
                       gr.name,
                       gr.html_url,
                       ga.login,
                       ga.html_url,
                       ga.avatar_url,
                       'APPROVED',
                       1,
                       :mainFileExtensions
                from indexer_exp.github_accounts ga
                join indexer_exp.github_repos gr on gr.id = :repoId
                where ga.id = :contributorId
                """, parameters);

        createContributionFromPullRequest(prId, contributor.user().getGithubUserId());

        return prId;
    }

    public Long createIssue(GithubRepo repo, UserAuthHelper.AuthenticatedUser contributor) {
        return createIssue(repo.getId(), CurrentDateProvider.now(), CurrentDateProvider.now().plusDays(1), "COMPLETED", contributor);
    }

    public Long createIssue(Long repoId, ZonedDateTime createdAt, ZonedDateTime closedAt, String status, UserAuthHelper.AuthenticatedUser contributor) {
        final var parameters = new HashMap<String, Object>();
        parameters.put("repoId", repoId);
        parameters.put("number", faker.random().nextInt(10));
        parameters.put("title", faker.lorem().sentence());
        parameters.put("body", faker.lorem().sentence());
        parameters.put("status", status);
        parameters.put("commentsCount", faker.random().nextInt(10));
        parameters.put("createdAt", createdAt);
        parameters.put("closedAt", closedAt);
        parameters.put("authorId", contributor.user().getGithubUserId());
        parameters.put("htmlUrl", faker.internet().url());

        final Long nextIssueId = databaseHelper.<Long>executeReadQuery(
                """
                        select id + 1  from indexer_exp.github_issues
                        order by id desc
                        limit 1
                        """, Map.of()
        );
        parameters.put("issueId", nextIssueId);

        createAccount(contributor);

        databaseHelper.executeQuery(
                """
                        INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url)
                        SELECT  :issueId,
                                gr.id,
                                :number,
                                :title,
                                cast(:status as indexer_exp.github_issue_status),
                                :createdAt,
                                :closedAt,
                                ga.id,
                                :htmlUrl,
                                :body,
                                :commentsCount,
                                gr.owner_login,
                                gr.name,
                                gr.html_url,
                                ga.login,
                                ga.html_url,
                                ga.avatar_url
                        FROM indexer_exp.github_accounts ga
                        JOIN indexer_exp.github_repos gr ON gr.id = :repoId
                        WHERE ga.id = :authorId
                        """,
                parameters
        );

        return nextIssueId;
    }

    public void addLabelToIssue(Long issueId, String label, ZonedDateTime addedToIssueAt) {
        final var labelParameters = new HashMap<String, Object>();
        labelParameters.put("id", faker.random().nextInt(200));
        labelParameters.put("label", label);
        labelParameters.put("description", faker.lorem().sentence());

        databaseHelper.executeQuery(
                """
                        INSERT INTO indexer_exp.github_labels (id, name, description)
                        VALUES (:id, :label, :description)
                        ON CONFLICT DO NOTHING;
                        """,
                labelParameters
        );

        final var issueLabelParameters = new HashMap<String, Object>();
        issueLabelParameters.put("issueId", issueId);
        issueLabelParameters.put("label", label);
        issueLabelParameters.put("createdAt", addedToIssueAt);
        issueLabelParameters.put("updatedAt", addedToIssueAt);
        databaseHelper.executeQuery(
                """
                                    INSERT INTO indexer_exp.github_issues_labels (issue_id, label_id, tech_created_at, tech_updated_at)
                                    SELECT :issueId, gl.id, :createdAt, :updatedAt
                                    FROM indexer_exp.github_labels gl
                                    WHERE gl.name = :label
                        """,
                issueLabelParameters
        );
    }

    public void assignIssueToContributor(Long issueId, Long contributorId) {
        createAccount(contributorId);

        databaseHelper.executeQuery(
                """
                        INSERT INTO indexer_exp.github_issues_assignees (issue_id, user_id) VALUES (:issueId, :contributorId)
                        ON CONFLICT DO NOTHING;
                        """, Map.of(
                        "contributorId", contributorId,
                        "issueId", issueId)
        );

        createContributionFromIssue(issueId, contributorId);
    }

    private void createContributionFromPullRequest(Long prId, Long contributorId) {
        databaseHelper.executeQuery("""
                insert into indexer_exp.contributions(id, repo_id, contributor_id, type, status, pull_request_id, created_at, completed_at, github_number, github_status, github_title, github_html_url, github_body, github_comments_count, repo_owner_login, repo_name, repo_html_url, github_author_id, github_author_login, github_author_html_url, github_author_avatar_url, contributor_login, contributor_html_url, contributor_avatar_url, pr_review_state, main_file_extensions)
                select  :contributionId,
                        gpr.repo_id,
                        ga.id,
                        'PULL_REQUEST',
                        'COMPLETED',
                        gpr.id,
                        gpr.created_at,
                        gpr.closed_at,
                        gpr.number,
                        'MERGED',
                        gpr.title,
                        gpr.html_url,
                        gpr.body,
                        gpr.comments_count,
                        gpr.repo_owner_login,
                        gpr.repo_name,
                        gpr.repo_html_url,
                        gpr.author_id,
                        gpr.author_login,
                        gpr.author_html_url,
                        gpr.author_avatar_url,
                        ga.login,
                        ga.html_url,
                        ga.avatar_url,
                        'APPROVED',
                        gpr.main_file_extensions
                from indexer_exp.github_pull_requests gpr
                join indexer_exp.github_accounts ga on ga.id = :contributorId
                where gpr.id = :prId
                on conflict do nothing;
                """, Map.of("contributionId", faker.random().hex(),
                "prId", prId,
                "contributorId", contributorId));

        addRepoContributorFromPullRequest(prId, contributorId);
    }

    private void createContributionFromIssue(Long issueId, Long contributorId) {
        databaseHelper.executeQuery("""
                      insert into indexer_exp.contributions(id, repo_id, contributor_id, type, status, issue_id, created_at, completed_at, github_number, github_status, github_title, github_html_url, github_body, github_comments_count, repo_owner_login, repo_name, repo_html_url, github_author_id, github_author_login, github_author_html_url, github_author_avatar_url, contributor_login, contributor_html_url, contributor_avatar_url, pr_review_state, main_file_extensions)
                        select  :contributionId,
                                gi.repo_id,
                                ga.id,
                                'ISSUE',
                                'COMPLETED',
                                gi.id,
                                gi.created_at,
                                gi.closed_at,
                                gi.number,
                                'COMPLETED',
                                gi.title,
                                gi.html_url,
                                gi.body,
                                gi.comments_count,
                                gi.repo_owner_login,
                                gi.repo_name,
                                gi.repo_html_url,
                                ga.id,
                                ga.login,
                                ga.html_url,
                                ga.avatar_url,
                                ga.login,
                                ga.html_url,
                                ga.avatar_url,
                                'APPROVED',
                                null
                        from indexer_exp.github_issues gi
                        join indexer_exp.github_accounts ga on ga.id = :contributorId
                        where gi.id = :issueId
                        on conflict do nothing;
                """, Map.of("contributionId", faker.random().hex(),
                "issueId", issueId,
                "contributorId", contributorId));

        addRepoContributorFromIssue(issueId, contributorId);
    }

    private void addRepoContributorFromIssue(Long issueId, Long contributorId) {
        databaseHelper.executeQuery("""
                insert into indexer_exp.repos_contributors(repo_id, contributor_id, completed_contribution_count, total_contribution_count)
                select distinct repo_id, :contributorId, case when c.status = 'COMPLETED' THEN 1 ELSE 0 END, 1
                from indexer_exp.contributions c
                where c.issue_id = :issueId
                on conflict (repo_id, contributor_id) do update set
                    completed_contribution_count = repos_contributors.completed_contribution_count + excluded.completed_contribution_count,
                    total_contribution_count = repos_contributors.total_contribution_count + excluded.total_contribution_count;
                """, Map.of("issueId", issueId,
                "contributorId", contributorId));
    }

    private void addRepoContributorFromCodeReview(String codeReviewId, Long contributorId) {
        databaseHelper.executeQuery("""
                insert into indexer_exp.repos_contributors(repo_id, contributor_id, completed_contribution_count, total_contribution_count)
                select distinct repo_id, :contributorId, case when c.status = 'COMPLETED' THEN 1 ELSE 0 END, 1
                from indexer_exp.contributions c
                where c.code_review_id = :codeReviewId
                on conflict (repo_id, contributor_id) do update set
                    completed_contribution_count = repos_contributors.completed_contribution_count + excluded.completed_contribution_count,
                    total_contribution_count = repos_contributors.total_contribution_count + excluded.total_contribution_count;
                """, Map.of("codeReviewId", codeReviewId,
                "contributorId", contributorId));
    }

    private void addRepoContributorFromPullRequest(Long pullRequestId, Long contributorId) {
        databaseHelper.executeQuery("""
                insert into indexer_exp.repos_contributors(repo_id, contributor_id, completed_contribution_count, total_contribution_count)
                select distinct repo_id, :contributorId, case when c.status = 'COMPLETED' THEN 1 ELSE 0 END, 1
                from indexer_exp.contributions c
                where c.pull_request_id = :pullRequestId
                on conflict (repo_id, contributor_id) do update set
                    completed_contribution_count = repos_contributors.completed_contribution_count + excluded.completed_contribution_count,
                    total_contribution_count = repos_contributors.total_contribution_count + excluded.total_contribution_count;
                """, Map.of("pullRequestId", pullRequestId,
                "contributorId", contributorId));
    }

    public void createCodeReview(GithubRepo repo, Long prId, UserAuthHelper.AuthenticatedUser contributor) {
        final var id = faker.random().hex();
        final var prNumber = faker.random().nextInt(1000);
        final var parameters = new HashMap<String, Object>();

        parameters.put("id", id);
        parameters.put("repoId", repo.getId());
        parameters.put("repoOwnerLogin", repo.getOwner());
        parameters.put("repoName", repo.getName());
        parameters.put("repoHtmlUrl", repo.getHtmlUrl());
        parameters.put("codeReviewId", faker.random().nextLong());
        parameters.put("prId", prId);
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
                
                insert into indexer_exp.github_code_reviews(id, pull_request_id, author_id, state, requested_at, submitted_at, number, title, html_url, body, comments_count, repo_owner_login, repo_name, repo_id, repo_html_url, author_login, author_html_url, author_avatar_url)
                values (:codeReviewId, :prId, :contributorId, 'APPROVED', :createdAt, :completedAt, :githubNumber, :githubTitle, :githubHtmlUrl, :githubBody, :githubCommentsCount, :repoOwnerLogin, :repoName, :repoId, :repoHtmlUrl, :contributorLogin, :contributorHtmlUrl, :contributorAvatarUrl);
                
                insert into indexer_exp.contributions(id, repo_id, contributor_id, type, status, code_review_id, created_at, completed_at, github_number, github_status, github_title, github_html_url, github_body, github_comments_count, repo_owner_login, repo_name, repo_html_url, github_author_id, github_author_login, github_author_html_url, github_author_avatar_url, contributor_login, contributor_html_url, contributor_avatar_url, pr_review_state, main_file_extensions)
                values (:id, :repoId, :contributorId, 'CODE_REVIEW', 'COMPLETED', :codeReviewId, :createdAt, :completedAt, :githubNumber, 'APPROVED', :githubTitle, :githubHtmlUrl, :githubBody, :githubCommentsCount, :repoOwnerLogin, :repoName, :repoHtmlUrl, :contributorId, :contributorLogin, :contributorHtmlUrl, :contributorAvatarUrl, :contributorLogin, :contributorHtmlUrl, :contributorAvatarUrl, 'APPROVED', null);
                """, parameters);

        addRepoContributorFromCodeReview(id, contributor.user().getGithubUserId());
    }
}
