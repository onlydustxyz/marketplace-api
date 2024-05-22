package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoCommitteeQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BoCommitteeQueryRepository extends JpaRepository<BoCommitteeQueryEntity, BoCommitteeQueryRepository> {

    @Query(nativeQuery = true, value = """
                with applications as (select distinct cpa.user_id, cpa.project_id, cpa.committee_id
                                        from committees c
                                                 left join committee_project_answers cpa on cpa.committee_id = c.id)
                  select c.id,
                         c.name,
                         c.status,
                         c.start_date,
                         c.end_date,
                         (select jsonb_agg(
                                         jsonb_build_object('id', cpq.id, 'question', cpq.question, 'required', cpq.required)
                                 )
                          from committee_project_questions cpq
                          where cpq.committee_id = c.id) project_questions,
                         s.id       as                   sponsor_id,
                         s.name     as                   sponsor_name,
                         s.url      as                   sponsor_url,
                         s.logo_url as                   sponsor_logo_url,
                         case
                             when a.committee_id is null then null
                             else
                                 jsonb_agg(
                                         jsonb_build_object('projectId', p.id, 'projectName', p.name, 'projectLogoUrl', p.logo_url,
                                                            'projectSlug',
                                                            p.slug, 'projectShortDescription', p.short_description, 'projectVisibility',
                                                            p.visibility,
                                                            'userId', u.id, 'userAvatarUrl', u.github_avatar_url, 'userGithubLogin',
                                                            u.github_login, 'userGithubId', u.github_user_id)
                                 ) end                   project_applications
                  from committees c
                           left join sponsors s on s.id = c.sponsor_id
                           left join applications a on a.committee_id = c.id
                           left join projects p on p.id = a.project_id
                           left join iam.users u on u.id = a.user_id
                  where c.id = :committeeId
                  group by c.id, c.name, c.status, c.start_date, c.end_date, s.id, s.name, s.url, s.logo_url, a.committee_id
            """)
    Optional<BoCommitteeQueryEntity> findById(UUID committeeId);
}
