call drop_pseudo_projection('bi', 'project_global_data');
call drop_pseudo_projection('bi', 'project_budget_data');
call drop_pseudo_projection('bi', 'reward_data');

call create_pseudo_projection('bi', 'reward_data', $$
select r.id                                                                                             as reward_id,
       r.requested_at                                                                                   as timestamp,
       date_trunc('day', r.requested_at)                                                                as day_timestamp,
       date_trunc('week', r.requested_at)                                                               as week_timestamp,
       date_trunc('month', r.requested_at)                                                              as month_timestamp,
       date_trunc('quarter', r.requested_at)                                                            as quarter_timestamp,
       date_trunc('year', r.requested_at)                                                               as year_timestamp,
       r.recipient_id                                                                                   as contributor_id,
       r.requestor_id                                                                                   as requestor_id,
       r.project_id                                                                                     as project_id,
       p.slug                                                                                           as project_slug,
       rsd.amount_usd_equivalent                                                                        as usd_amount,
       r.amount                                                                                         as amount,
       r.currency_id                                                                                    as currency_id,
       array_agg(distinct ri.contribution_uuid) filter ( where ri.contribution_uuid is not null )       as contribution_uuids,
       (jsonb_agg(distinct jsonb_build_object('id', receipts.id,
                                              'createdAt', receipts.created_at,
                                              'network', receipts.network::text,
                                              'thirdPartyName', receipts.third_party_name,
                                              'thirdPartyAccountNumber', receipts.third_party_account_number,
                                              'transactionReference', receipts.transaction_reference))
        filter ( where receipts.transaction_reference is not null ))[0]                                 as receipt,
       array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
       array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
       array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
       array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
       string_agg(currencies.name || ' ' || currencies.code, ' ')                                       as search
from rewards r
         join accounting.reward_status_data rsd ON rsd.reward_id = r.id
         join projects p on p.id = r.project_id
         join currencies on currencies.id = r.currency_id
         left join projects_ecosystems pe on pe.project_id = r.project_id
         left join m_programs_projects pp on pp.project_id = r.project_id
         left join projects_project_categories ppc on ppc.project_id = r.project_id
         left join reward_items ri on r.id = ri.reward_id
         left join indexer_exp.contributions c on c.contributor_id = ri.recipient_id and
                                                  c.repo_id = ri.repo_id and
                                                  c.github_number = ri.number and
                                                  c.type::text::contribution_type = ri.type
         left join language_file_extensions lfe on lfe.extension = any (c.main_file_extensions)
         left join accounting.rewards_receipts rr on rr.reward_id = r.id
         left join accounting.receipts receipts on receipts.id = rr.receipt_id
group by r.id,
         r.requested_at,
         r.recipient_id,
         r.project_id,
         rsd.amount_usd_equivalent,
         r.amount,
         r.currency_id,
         p.slug
$$, 'reward_id');

create index p_reward_data_contributor_id_project_id_index on bi.p_reward_data (contributor_id, project_id);
create unique index p_reward_data_contributor_id_reward_id_usd_amount_uindex on bi.p_reward_data (contributor_id, reward_id, usd_amount);

create index bi_p_reward_data_project_id_timestamp_idx on bi.p_reward_data (project_id, timestamp, currency_id);
create index bi_p_reward_data_project_id_day_timestamp_idx on bi.p_reward_data (project_id, day_timestamp, currency_id);
create index bi_p_reward_data_project_id_week_timestamp_idx on bi.p_reward_data (project_id, week_timestamp, currency_id);
create index bi_p_reward_data_project_id_month_timestamp_idx on bi.p_reward_data (project_id, month_timestamp, currency_id);
create index bi_p_reward_data_project_id_quarter_timestamp_idx on bi.p_reward_data (project_id, quarter_timestamp, currency_id);
create index bi_p_reward_data_project_id_year_timestamp_idx on bi.p_reward_data (project_id, year_timestamp, currency_id);
create index bi_p_reward_data_project_id_timestamp_idx_inv on bi.p_reward_data (timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_day_timestamp_idx_inv on bi.p_reward_data (day_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_week_timestamp_idx_inv on bi.p_reward_data (week_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_month_timestamp_idx_inv on bi.p_reward_data (month_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_quarter_timestamp_idx_inv on bi.p_reward_data (quarter_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_year_timestamp_idx_inv on bi.p_reward_data (year_timestamp, project_id, currency_id);

create index bi_p_reward_data_contributor_id_timestamp_idx on bi.p_reward_data (contributor_id, timestamp, currency_id);
create index bi_p_reward_data_contributor_id_day_timestamp_idx on bi.p_reward_data (contributor_id, day_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_week_timestamp_idx on bi.p_reward_data (contributor_id, week_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_month_timestamp_idx on bi.p_reward_data (contributor_id, month_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_quarter_timestamp_idx on bi.p_reward_data (contributor_id, quarter_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_year_timestamp_idx on bi.p_reward_data (contributor_id, year_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_timestamp_idx_inv on bi.p_reward_data (timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_day_timestamp_idx_inv on bi.p_reward_data (day_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_week_timestamp_idx_inv on bi.p_reward_data (week_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_month_timestamp_idx_inv on bi.p_reward_data (month_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_quarter_timestamp_idx_inv on bi.p_reward_data (quarter_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_year_timestamp_idx_inv on bi.p_reward_data (year_timestamp, contributor_id, currency_id);



call create_pseudo_projection('bi', 'project_global_data', $$
SELECT p.id                                                                                                  as project_id,
       p.slug                                                                                                as project_slug,
       p.created_at                                                                                          as created_at,
       p.rank                                                                                                as rank,
       jsonb_build_object('id', p.id,
                          'slug', p.slug,
                          'name', p.name,
                          'logoUrl', p.logo_url,
                          'shortDescription', p.short_description,
                          'hiring', p.hiring,
                          'visibility', p.visibility)                                                        as project,
       p.name                                                                                                as project_name,
       p.visibility                                                                                          as project_visibility,
       array_agg(distinct uleads.id) filter ( where uleads.id is not null )                                  as project_lead_ids,
       array_agg(distinct uinvleads.id) filter ( where uinvleads.id is not null )                            as invited_project_lead_ids,
       array_agg(distinct pc.id) filter ( where pc.id is not null )                                          as project_category_ids,
       array_agg(distinct pc.slug) filter ( where pc.slug is not null )                                      as project_category_slugs,
       array_agg(distinct l.id) filter ( where l.id is not null )                                            as language_ids,
       array_agg(distinct l.slug) filter ( where l.slug is not null )                                        as language_slugs,
       array_agg(distinct e.id) filter ( where e.id is not null )                                            as ecosystem_ids,
       array_agg(distinct e.slug) filter ( where e.slug is not null )                                        as ecosystem_slugs,
       array_agg(distinct prog.id) filter ( where prog.id is not null )                                      as program_ids,
       array_agg(distinct pgr.github_repo_id) filter ( where pgr.github_repo_id is not null )                as repo_ids,
       array_agg(distinct pt.tag) filter ( where pt.tag is not null )                                        as tags,

       jsonb_agg(distinct jsonb_build_object('id', uleads.id,
                                             'login', uleads.github_login,
                                             'githubUserId', uleads.github_user_id,
                                             'avatarUrl', user_avatar_url(uleads.github_user_id, uleads.github_avatar_url)
                          )) filter ( where uleads.id is not null )                                          as leads,

       jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                             'slug', pc.slug,
                                             'name', pc.name,
                                             'description', pc.description,
                                             'iconSlug', pc.icon_slug)) filter ( where pc.id is not null )   as categories,

       jsonb_agg(distinct jsonb_build_object('id', l.id,
                                             'slug', l.slug,
                                             'name', l.name,
                                             'logoUrl', l.logo_url,
                                             'bannerUrl', l.banner_url)) filter ( where l.id is not null )   as languages,

       jsonb_agg(distinct jsonb_build_object('id', e.id,
                                             'slug', e.slug,
                                             'name', e.name,
                                             'logoUrl', e.logo_url,
                                             'bannerUrl', e.banner_url,
                                             'url', e.url)) filter ( where e.id is not null )                as ecosystems,

       jsonb_agg(distinct jsonb_build_object('id', prog.id,
                                             'name', prog.name,
                                             'logoUrl', prog.logo_url)) filter ( where prog.id is not null ) as programs,

       count(distinct pgr.github_repo_id) > count(distinct agr.repo_id)                                      as has_repos_without_github_app_installed,

       concat(coalesce(string_agg(distinct uleads.github_login, ' '), ''), ' ',
              coalesce(string_agg(distinct p.name, ' '), ''), ' ',
              coalesce(string_agg(distinct p.slug, ' '), ''), ' ',
              coalesce(string_agg(distinct pc.name, ' '), ''), ' ',
              coalesce(string_agg(distinct l.name, ' '), ''), ' ',
              coalesce(string_agg(distinct e.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.code, ' '), ''), ' ',
              coalesce(string_agg(distinct prog.name, ' '), ''))                                             as search
FROM projects p
         LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
         LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
         LEFT JOIN project_languages pl ON pl.project_id = p.id
         LEFT JOIN languages l ON l.id = pl.language_id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
         LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
         LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
         LEFT JOIN programs prog ON prog.id = pp.program_id
         LEFT JOIN project_leads pleads ON pleads.project_id = p.id
         LEFT JOIN iam.users uleads ON uleads.id = pleads.user_id
         LEFT JOIN pending_project_leader_invitations ppli ON ppli.project_id = p.id
         LEFT JOIN iam.users uinvleads ON uinvleads.github_user_id = ppli.github_user_id
         LEFT JOIN projects_tags pt ON pt.project_id = p.id
         LEFT JOIN project_github_repos pgr ON pgr.project_id = p.id
         LEFT JOIN indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
         LEFT JOIN LATERAL (select distinct c.name, c.code
                            from bi.p_reward_data rd
                                     full outer join bi.p_project_grants_data gd on gd.project_id = rd.project_id
                                     join currencies c on c.id = coalesce(rd.currency_id, gd.currency_id)
                            where rd.project_id = p.id
                               or gd.project_id = p.id) currencies on true
GROUP BY p.id
$$, 'project_id');

create unique index on bi.p_project_global_data (project_slug);



call create_pseudo_projection('bi', 'project_budget_data', $$
SELECT p.id                                                                                                  as project_id,
       max(budgets.available_budget_usd)                                                                     as available_budget_usd,
       max(budgets.percent_spent_budget_usd)                                                                 as percent_spent_budget_usd,
       coalesce(jsonb_agg(distinct jsonb_build_object('availableBudgetUsd', budgets.available_budget_usd,
                                                      'percentSpentBudgetUsd', budgets.percent_spent_budget_usd,
                                                      'availableBudgetPerCurrency', budgets.available_budget_per_currency,
                                                      'percentSpentBudgetPerCurrency', budgets.percent_spent_budget_per_currency,
                                                      'grantedAmountUsd', budgets.granted_amount_usd,
                                                      'grantedAmountPerCurrency', budgets.granted_amount_per_currency,
                                                      'rewardedAmountUsd', budgets.rewarded_amount_usd,
                                                      'rewardedAmountPerCurrency', budgets.rewarded_amount_per_currency))
                filter ( where budgets.project_id is not null ), '[]'::jsonb) -> 0                           as budget

FROM projects p
         LEFT JOIN LATERAL (select gd.project_id                                                                as project_id,
                                   coalesce(sum(gd.current_usd_amount), 0)                                      as granted_amount_usd,   -- sum of all granted amount, using their USD equivalent at the time of the grant
                                   coalesce(sum(rd.usd_amount), 0)                                              as rewarded_amount_usd,  -- sum of all rewarded amount, using their USD equivalent at the time of the reward
                                   sum(coalesce(gd.current_usd_amount, 0) - coalesce(rd.current_usd_amount, 0)) as available_budget_usd, -- available budget in USD, using the current USD equivalent of each currency

                                   sum(coalesce(rd.current_usd_amount, 0)) /
                                   greatest(sum(coalesce(gd.current_usd_amount, 0)), 1)                         as percent_spent_budget_usd,

                                   jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                'amount', gd.amount,
                                                                'usdAmount',
                                                                gd.current_usd_amount))                         as granted_amount_per_currency,

                                   jsonb_agg(jsonb_build_object('currency', rd.currency,
                                                                'amount', rd.amount,
                                                                'usdAmount', rd.usd_amount))
                                   filter ( where rd.currency_id is not null )                                  as rewarded_amount_per_currency,

                                   jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                'amount', coalesce(gd.amount, 0) - coalesce(rd.amount, 0),
                                                                'usdAmount',
                                                                coalesce(gd.current_usd_amount, 0) -
                                                                coalesce(rd.current_usd_amount, 0)))            as available_budget_per_currency,

                                   jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                'amount', coalesce(gd.amount, 0) /
                                                                          coalesce(rd.amount, 1)))              as percent_spent_budget_per_currency

                            from (select gd.project_id                                             as project_id,
                                         c.id                                                      as currency_id,
                                         jsonb_build_object('id', c.id,
                                                            'code', c.code,
                                                            'name', c.name,
                                                            'decimals', c.decimals,
                                                            'logoUrl', c.logo_url)                 as currency,
                                         sum(gd.amount)                                            as amount,
                                         accounting.usd_equivalent_at(sum(gd.amount), c.id, now()) as current_usd_amount
                                  from bi.p_project_grants_data gd
                                           join currencies c on c.id = gd.currency_id
                                  group by gd.project_id, c.id) gd

                                     left join (select rd.project_id                                             as project_id,
                                                       c.id                                                      as currency_id,
                                                       jsonb_build_object('id', c.id,
                                                                          'code', c.code,
                                                                          'name', c.name,
                                                                          'decimals', c.decimals,
                                                                          'logoUrl', c.logo_url)                 as currency,
                                                       sum(rd.usd_amount)                                        as usd_amount,
                                                       sum(rd.amount)                                            as amount,
                                                       accounting.usd_equivalent_at(sum(rd.amount), c.id, now()) as current_usd_amount
                                                from bi.p_reward_data rd
                                                         join currencies c on c.id = rd.currency_id
                                                group by rd.project_id, c.id) rd on gd.project_id = rd.project_id and gd.currency_id = rd.currency_id
                            group by gd.project_id ) budgets on budgets.project_id = p.id
GROUP BY p.id
$$, 'project_id');


create unique index on bi.p_project_budget_data (project_id, available_budget_usd, percent_spent_budget_usd);