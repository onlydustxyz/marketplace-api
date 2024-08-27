package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.github.HackathonGithubIssueItemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface HackathonGithubIssueItemReadRepository extends Repository<HackathonGithubIssueItemReadEntity, Long> {

    @Query(value = """
            select gi.*,
                   assignees.users                                                                                   assignees,
                   applicants.users                                                                                  applicants,
                   jsonb_build_object('id', gr.id, 'owner', gr.owner_login, 'name', gr.name, 'htmlUrl', gr.html_url) repo,
                   labels.strings                                                                                    labels,
                   hackathon_issues.projects,
                   jsonb_build_object(
                                       'userId', u.id,
                                       'githubUserId', author_account.id,
                                       'login', author_account.login,
                                       'avatarUrl', user_avatar_url(author_account.id, author_account.avatar_url)
                                    ) author
            from indexer_exp.github_issues gi
                     join (select hi.issue_id,
                                  hi.hackathon_id,
                                  jsonb_agg(
                                          jsonb_build_object(
                                                  'id', p.id,
                                                  'name', p.name,
                                                  'slug', p.slug,
                                                  'logoUrl', p.logo_url
                                          )
                                  ) projects,
                                  array_agg(p.id) project_ids
                           from hackathon_issues hi
                                    left join projects p on p.id = any (hi.project_ids)
                           group by hi.issue_id, hi.hackathon_id) hackathon_issues on hackathon_issues.issue_id = gi.id
                     join indexer_exp.github_repos gr on gr.id = gi.repo_id
                     JOIN indexer_exp.github_accounts author_account on gi.author_id = author_account.id
                     left join iam.users u on u.github_user_id = author_account.id
                     LEFT JOIN (select gia.issue_id,
                                       jsonb_agg(
                                               jsonb_build_object(
                                                       'githubUserId', ga.id,
                                                       'login', ga.login,
                                                       'avatarUrl', ga.avatar_url
                                               )
                                       ) users
                                from indexer_exp.github_issues_assignees gia
                                         LEFT JOIN indexer_exp.github_accounts ga on ga.id = gia.user_id
                                group by gia.issue_id) assignees on assignees.issue_id = gi.id
                     left join (select a.issue_id,
                                       jsonb_agg(
                                               jsonb_build_object(
                                                       'githubUserId', ga2.id,
                                                       'login', ga2.login,
                                                       'avatarUrl', ga2.avatar_url
                                               )
                                       ) users
                                from applications a
                                         join indexer_exp.github_accounts ga2 on ga2.id = a.applicant_id
                                group by a.issue_id) applicants on applicants.issue_id = gi.id
                     left join (select gil.issue_id, jsonb_agg(gl.name) strings
                                from indexer_exp.github_issues_labels gil
                                         join indexer_exp.github_labels gl on gil.label_id = gl.id
                                group by gil.issue_id) labels on labels.issue_id = gi.id
            where hackathon_issues.hackathon_id = :hackathonId
            and
                (:search IS NULL OR gi.title ILIKE '%' || CAST(:search as text) || '%')
                AND (:projectIds IS NULL OR hackathon_issues.project_ids && cast(:projectIds as uuid[]))
                AND (:isAssigned IS NULL OR
                    (:isAssigned = TRUE AND assignees.users IS NOT NULL) OR
                    (:isAssigned = FALSE AND assignees.users IS NULL)
                )
            """, nativeQuery = true)
    Page<HackathonGithubIssueItemReadEntity> findHackathonIssues(UUID hackathonId,
                                                                 String search,
                                                                 UUID[] projectIds,
                                                                 Boolean isAssigned,
                                                                 Pageable pageable);
}
