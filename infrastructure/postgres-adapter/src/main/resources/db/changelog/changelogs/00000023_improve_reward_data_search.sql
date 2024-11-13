-- call create_pseudo_projection('bi', 'reward_data', $$...$$);
create or replace view bi.v_reward_data as
SELECT v.*, md5(v::text) as hash
FROM (select r.id                                                                                             as reward_id,
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
                                                    'createdAt', receipts.created_at::timestamptz,
                                                    'network', receipts.network::text,
                                                    'thirdPartyName', receipts.third_party_name,
                                                    'thirdPartyAccountNumber', receipts.third_party_account_number,
                                                    'transactionReference', receipts.transaction_reference))
              filter ( where receipts.transaction_reference is not null ))[0]                                 as receipt,
             array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
             array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
             array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
             array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
             concat('#', substring(r.id::text, 0, 5), ' ',
                    rsd.amount_usd_equivalent::int::text, ' ',
                    p.name, ' ',
                    requestor.login, ' ',
                    recipient.login, ' ',
                    string_agg(distinct c.github_title, ' '), ' ',
                    string_agg(distinct c.github_number::text, ' '), ' ',
                    currencies.name, ' ',
                    currencies.code)                                                                          as search
      from rewards r
               join accounting.reward_status_data rsd ON rsd.reward_id = r.id
               join projects p on p.id = r.project_id
               join currencies on currencies.id = r.currency_id
               left join projects_ecosystems pe on pe.project_id = r.project_id
               left join m_programs_projects pp on pp.project_id = r.project_id
               left join projects_project_categories ppc on ppc.project_id = r.project_id
               left join reward_items ri on r.id = ri.reward_id
               left join indexer_exp.contributions c on c.contribution_uuid = ri.contribution_uuid
               left join language_file_extensions lfe on lfe.extension = any (c.main_file_extensions)
               left join accounting.rewards_receipts rr on rr.reward_id = r.id
               left join accounting.receipts receipts on receipts.id = rr.receipt_id
               left join iam.all_indexed_users requestor on requestor.user_id = r.requestor_id
               left join iam.all_indexed_users recipient on recipient.github_user_id = r.recipient_id
      group by r.id,
               r.requested_at,
               r.recipient_id,
               r.project_id,
               rsd.amount_usd_equivalent,
               r.amount,
               r.currency_id,
               p.slug,
               p.name,
               requestor.login,
               recipient.login,
               currencies.name,
               currencies.code) v;

call refresh_pseudo_projection('bi', 'reward_data', 'reward_id');