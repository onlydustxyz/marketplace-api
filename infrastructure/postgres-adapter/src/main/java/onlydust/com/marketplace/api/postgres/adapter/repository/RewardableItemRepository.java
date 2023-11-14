package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardableItemViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RewardableItemRepository extends JpaRepository<RewardableItemViewEntity, String> {

    @Query(value = """
            with get_pr as (select gpr.number,
                                   gpr.id,
                                   gpr.html_url,
                                   gpr.title,
                                   gpr.status,
            --                        gpr.draft,
                                   gpr.created_at                         start_date,
                                   coalesce(gpr.closed_at, gpr.merged_at) end_date,
                                   (select count(c.pull_request_id)
                                    from github_pull_request_commits c
                                    where c.pull_request_id = gpr.id)     commits_count
                            from indexer_exp.github_pull_requests gpr),
                 get_issue as (select gi.number,
                                      gi.id,
                                      gi.status,
                                      gi.html_url,
                                      gi.title,
                                      gi.created_at start_date,
                                      gi.closed_at  end_date,
                                      gi.comments_count
                               from indexer_exp.github_issues gi),
                 get_code_review as (select gpr.number,
                                            gcr.id,
                                            gpr.status,
                                            gpr.html_url,
                                            gpr.title,
                                            gcr.state        outcome,
                                            gpr.created_at   start_date,
                                            gcr.submitted_at end_date
                                     from indexer_exp.github_code_reviews gcr
                                              left join indexer_exp.github_pull_requests gpr
                                                        on gpr.id = gcr.pull_request_id)

            select c.id,
                   c.type,
                   coalesce(cast(pull_request.status as text), cast(issue.status as text), cast(code_review.status as text)) status,
                   false                                                                                                     draft,
                   coalesce(pull_request.number, issue.number, code_review.number)                                           number,
                   coalesce(pull_request.html_url, issue.html_url,
                            code_review.html_url)                                                                            html_url,
                   coalesce(pull_request.title, issue.title, code_review.title)                                              title,
                   repo.name                                                                                                 repo_name,
                   coalesce(pull_request.start_date, issue.start_date,
                            code_review.start_date)                                                                          start_date,
                   coalesce(pull_request.end_date, issue.end_date,
                            code_review.end_date)                                                                            end_date,
                   coalesce(pull_request.html_url, issue.html_url,
                            code_review.html_url)                                                                            html_url,
                   pull_request.commits_count,
                   (select count(c.pull_request_id)
                    from github_pull_request_commits c
                    where c.pull_request_id = pull_request.id
                      and c.author_id = :githubUserId)                                                                      user_commits_count,
                   issue.comments_count,
                   code_review.outcome                                                                                       cr_outcome
            from public.project_github_repos pgr
                     join indexer_exp.contributions c on c.repo_id = pgr.github_repo_id
                     left join get_code_review code_review on code_review.id = c.code_review_id
                     left join get_issue issue on issue.id = c.issue_id
                     left join get_pr pull_request on pull_request.id = c.pull_request_id
                     left join indexer_exp.github_repos repo on repo.id = pgr.github_repo_id
            where pgr.project_id = :projectId
              and c.contributor_id = :githubUserId
              and c.id not in (select ic.contribution_id from public.ignored_contributions ic where ic.project_id = :projectId)
              and (coalesce(:contributionType) is null or cast(c.type as text) = cast(:contributionType as text))
               and (coalesce(:search) is null
                    or coalesce(pull_request.title, issue.title, code_review.title) ilike '%' || cast(:search as text) || '%')
                    or cast(coalesce(pull_request.number, issue.number, code_review.number) as text) ilike '%' || cast(:search as text) || '%'
             order by coalesce(pull_request.start_date, issue.start_date,code_review.start_date) desc
              offset :offset limit :limit
              """, nativeQuery = true)
    List<RewardableItemViewEntity> findByProjectIdAndGithubUserId(final @Param("projectId") UUID projectId,
                                                                  final @Param("githubUserId") Long githubUserId,
                                                                  final @Param("contributionType") String contributionType,
                                                                  final @Param("search") String search,
                                                                  final @Param("offset") int offset,
                                                                  final @Param("limit") int limit);


    @Query(value = """
                        with get_pr as (select gpr.number,
                                   gpr.id,
                                   gpr.title,
                                   gpr.created_at                         start_date
                            from indexer_exp.github_pull_requests gpr),
                 get_issue as (select gi.number,
                                      gi.id,
                                      gi.title,
                                      gi.created_at start_date
                               from indexer_exp.github_issues gi),
                 get_code_review as (select gpr.number,
                                            gcr.id,
                                            gpr.title,
                                            gpr.created_at   start_date
                                     from indexer_exp.github_code_reviews gcr
                                              left join indexer_exp.github_pull_requests gpr
                                                        on gpr.id = gcr.pull_request_id)
            select count(c.id)
            from public.project_github_repos pgr
                     join indexer_exp.contributions c on c.repo_id = pgr.github_repo_id
                     left join get_code_review code_review on code_review.id = c.code_review_id
                     left join get_issue issue on issue.id = c.issue_id
                     left join get_pr pull_request on pull_request.id = c.pull_request_id
                     left join indexer_exp.github_repos repo on repo.id = pgr.github_repo_id
            where pgr.project_id = :projectId
              and c.contributor_id = :githubUserId
              and c.id not in (select ic.contribution_id from public.ignored_contributions ic where ic.project_id = :projectId)
              and (coalesce(:contributionType) is null or cast(c.type as text) = cast(:contributionType as text))
               and (coalesce(:search) is null
                    or coalesce(pull_request.title, issue.title, code_review.title) ilike '%' || cast(:search as text) || '%')
                    or cast(coalesce(pull_request.number, issue.number, code_review.number) as text) ilike '%' || cast(:search as text) || '%'
              """, nativeQuery = true)
    Long countByProjectIdAndGithubUserId(final @Param("projectId") UUID projectId,
                                         final @Param("githubUserId") Long githubUserId,
                                         final @Param("contributionType") String contributionType,
                                         final @Param("search") String search);
}
