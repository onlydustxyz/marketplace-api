package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoCommitteeQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BoCommitteeQueryRepository extends JpaRepository<BoCommitteeQueryEntity, BoCommitteeQueryRepository> {

    @Query(nativeQuery = true, value = """
            with a_ids as (select distinct cpa.user_id, cpa.project_id, cpa.committee_id
                           from committees c
                                    left join committee_project_answers cpa on cpa.committee_id = c.id)
            select c.id,
                   c.name,
                   c.status,
                   c.application_start_date,
                   c.application_end_date,
                   c.vote_per_jury,
                   (select jsonb_agg(
                                   jsonb_build_object('id', cpq.id, 'question', cpq.question, 'required', cpq.required)
                                   order by rank
                           )
                    from committee_project_questions cpq
                    where cpq.committee_id = c.id) project_questions,
                   s.id       as                   sponsor_id,
                   s.name     as                   sponsor_name,
                   s.url      as                   sponsor_url,
                   s.logo_url as                   sponsor_logo_url,
                   juries.members                  juries,
                   applications.user_project       project_applications,
                   (select jsonb_agg(
                                   jsonb_build_object('id', cjc.id, 'criteria', cjc.criteria)
                                   order by rank
                           )
                    from committee_jury_criteria cjc
                    where cjc.committee_id = c.id) jury_criteria,
                    jury_assignments.user_projects  jury_assignment_votes
            from committees c
                     left join sponsors s on s.id = c.sponsor_id
                     left join (select jsonb_agg(
                                               jsonb_build_object(
                                                       'userId', j.id, 'userAvatarUrl', j.github_avatar_url, 'userGithubLogin',
                                                       j.github_login, 'userGithubId', j.github_user_id
                                               )
                                       ) members
                                from committee_juries cj
                                         join iam.users j on j.id = cj.user_id
                                where cj.committee_id = :committeeId) juries on true
                     left join (select jsonb_agg(
                                               jsonb_build_object('projectId', p.id, 'projectName', p.name, 'projectLogoUrl',
                                                                  p.logo_url,
                                                                  'projectSlug',
                                                                  p.slug, 'projectShortDescription', p.short_description,
                                                                  'projectVisibility',
                                                                  p.visibility,
                                                                  'userId', u.id, 'userAvatarUrl', u.github_avatar_url,
                                                                  'userGithubLogin',
                                                                  u.github_login, 'userGithubId', u.github_user_id)) user_project
                                from a_ids a
                                         left join projects p on p.id = a.project_id
                                         left join iam.users u on u.id = a.user_id
                                where a.committee_id = :committeeId) applications on true
                    left join(select jsonb_agg(
                                                      jsonb_build_object(
                                                              'userId', jury.id, 'userAvatarUrl', jury.github_avatar_url,
                                                              'userGithubLogin', jury.github_login, 'userGithubId', jury.github_user_id,
                                                              'projectId', p_assigned.id, 'projectName', p_assigned.name, 'projectLogoUrl',
                                                              p_assigned.logo_url,
                                                              'projectSlug',
                                                              p_assigned.slug, 'projectShortDescription', p_assigned.short_description,
                                                              'projectVisibility',
                                                              p_assigned.visibility,
                                                              'score', cjv.score
                                                      )) user_projects
                                       from committee_jury_votes cjv
                                                join iam.users jury on jury.id = cjv.user_id
                                                join projects p_assigned on p_assigned.id = cjv.project_id
                                       where cjv.committee_id = :committeeId ) jury_assignments on true
            where c.id = :committeeId
            """)
    Optional<BoCommitteeQueryEntity> findById(UUID committeeId);
}
