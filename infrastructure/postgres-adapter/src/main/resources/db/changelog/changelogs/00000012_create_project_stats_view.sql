CREATE OR REPLACE VIEW public.project_stats_for_ranking_computation AS
with budget_stats as (select pb.project_id,
                             coalesce(sum(b.remaining_amount) filter ( where b.currency = 'usd' ),
                                      0) usd_remaining_amount,
                             coalesce(sum(b.remaining_amount) filter ( where b.currency = 'op' ),
                                      0) op_remaining_amount,
                             coalesce(sum(b.remaining_amount * cuq.price) filter ( where b.currency = 'op' ),
                                      0) op_dollars_equivalent_remaining_amount,
                             coalesce(sum(b.remaining_amount) filter ( where b.currency = 'stark' ),
                                      0) stark_remaining_amount,
                             coalesce(sum(b.remaining_amount * cuq.price) filter ( where b.currency = 'stark' ),
                                      0) stark_dollars_equivalent_remaining_amount,
                             coalesce(sum(b.remaining_amount) filter ( where b.currency = 'apt' ),
                                      0) apt_remaining_amount,
                             coalesce(sum(b.remaining_amount * cuq.price) filter ( where b.currency = 'apt' ),
                                      0) apt_dollars_equivalent_remaining_amount,
                             coalesce(sum(b.remaining_amount) filter ( where b.currency = 'eth' ),
                                      0) eth_remaining_amount,
                             coalesce(sum(b.remaining_amount * cuq.price) filter ( where b.currency = 'eth' ),
                                      0) eth_dollars_equivalent_remaining_amount
                      from projects_budgets pb
                               left join budgets b on pb.budget_id = b.id
                               left join crypto_usd_quotes cuq on cuq.currency = b.currency
                      group by pb.project_id),
     reward_stats as (select pr.project_id,
                             count(distinct pr.recipient_id)                                    distinct_recipient_number_last_1_month,
                             coalesce(sum(pr.amount) filter ( where pr.currency = 'usd' ), 0)   usd_spent_amount_last_1_month,
                             coalesce(sum(pr.amount) filter ( where pr.currency = 'op' ), 0)    op_spent_amount_last_1_month,
                             coalesce(sum(pr.amount * cuq.price) filter ( where pr.currency = 'op' ),
                                      0)                                                        op_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(pr.amount) filter ( where pr.currency = 'eth' ), 0)   eth_spent_amount_last_1_month,
                             coalesce(sum(pr.amount * cuq.price) filter ( where pr.currency = 'eth' ),
                                      0)                                                        eth_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(pr.amount) filter ( where pr.currency = 'apt' ), 0)   apt_spent_amount_last_1_month,
                             coalesce(sum(pr.amount * cuq.price) filter ( where pr.currency = 'apt' ),
                                      0)                                                        apt_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(pr.amount) filter ( where pr.currency = 'stark' ), 0) stark_spent_amount_last_1_month,
                             coalesce(sum(pr.amount * cuq.price) filter ( where pr.currency = 'stark' ),
                                      0)                                                        stark_spent_amount_dollars_equivalent_last_1_month
                      from payment_requests pr
                               left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                      where pr.requested_at > CURRENT_DATE - INTERVAL '1 months'
                      group by pr.project_id),
     contribution_stats as (select pgr.project_id,
                                   coalesce(sum(1) filter ( where c.type = 'PULL_REQUEST' ), 0) pr_count,
                                   coalesce(sum(1) filter ( where c.type = 'PULL_REQUEST' and
                                                                  c.created_at > CURRENT_DATE - INTERVAL '3 months'),
                                            0)                                                  pr_count_last_3_months,
                                   coalesce(sum(1)
                                            filter ( where c.type = 'PULL_REQUEST' and c.status = 'IN_PROGRESS' ),
                                            0)                                                  open_pr_count,
                                   coalesce(sum(1) filter ( where c.type = 'ISSUE' ), 0)        issue_count,
                                   coalesce(sum(1) filter ( where c.type = 'ISSUE' and
                                                                  c.created_at > CURRENT_DATE - INTERVAL '3 months'),
                                            0)                                                  issue_count_last_3_months,
                                   coalesce(sum(1) filter ( where c.type = 'ISSUE' and c.status = 'IN_PROGRESS'),
                                            0)                                                  open_issue_count,
                                   coalesce(sum(1) filter ( where c.type = 'CODE_REVIEW' ), 0)  cr_count,
                                   coalesce(sum(1) filter ( where c.type = 'CODE_REVIEW' and
                                                                  c.created_at > CURRENT_DATE - INTERVAL '3 months'),
                                            0)                                                  cr_count_last_3_months,
                                   coalesce(sum(1)
                                            filter ( where c.type = 'CODE_REVIEW' and c.status = 'IN_PROGRESS' ),
                                            0)                                                  open_cr_count,
                                   count(distinct c.contributor_id)                             contributor_count
                            from project_github_repos pgr
                                     join indexer_exp.github_repos gr
                                          on pgr.github_repo_id = gr.id and gr.visibility = 'PUBLIC'
                                     join indexer_exp.contributions c on c.repo_id = gr.id
                            group by pgr.project_id)
select pd2.project_id,
       pd2.created_at,
       cs.pr_count,
       cs.pr_count_last_3_months,
       cs.open_pr_count,
       cs.issue_count,
       cs.issue_count_last_3_months,
       cs.open_issue_count,
       cs.cr_count,
       cs.cr_count_last_3_months,
       cs.open_cr_count,
       cs.contributor_count,
       coalesce(rs.distinct_recipient_number_last_1_month, 0)             as distinct_recipient_number_last_1_months,
       coalesce(rs.usd_spent_amount_last_1_month, 0)                      as usd_spent_amount,
       coalesce(rs.op_spent_amount_last_1_month, 0)                       as op_spent_amount,
       coalesce(rs.op_spent_amount_dollars_equivalent_last_1_month, 0)    as op_spent_amount_dollars_equivalent,
       coalesce(rs.eth_spent_amount_last_1_month, 0)                      as eth_spent_amount,
       coalesce(rs.eth_spent_amount_dollars_equivalent_last_1_month, 0)   as eth_spent_amount_dollars_equivalent,
       coalesce(rs.apt_spent_amount_last_1_month, 0)                      as apt_spent_amount,
       coalesce(rs.apt_spent_amount_dollars_equivalent_last_1_month, 0)   as apt_spent_amount_dollars_equivalent,
       coalesce(rs.stark_spent_amount_last_1_month, 0)                    as stark_spent_amount,
       coalesce(rs.stark_spent_amount_dollars_equivalent_last_1_month, 0) as stark_spent_amount_dollars_equivalent,
       coalesce(rs.stark_spent_amount_dollars_equivalent_last_1_month +
                rs.op_spent_amount_dollars_equivalent_last_1_month +
                rs.apt_spent_amount_dollars_equivalent_last_1_month + rs.usd_spent_amount_last_1_month +
                rs.eth_spent_amount_dollars_equivalent_last_1_month,
                0)                                                        as total_dollars_equivalent_spent_last_1_month,
       coalesce(bs.usd_remaining_amount, 0)                               as usd_remaining_amount,
       coalesce(bs.op_remaining_amount, 0)                                as op_remaining_amount,
       coalesce(bs.stark_remaining_amount, 0)                             as stark_remaining_amount,
       coalesce(bs.apt_remaining_amount, 0)                               as apt_remaining_amount,
       coalesce(bs.eth_remaining_amount, 0)                               as eth_remaining_amount,
       coalesce(bs.apt_dollars_equivalent_remaining_amount + bs.usd_remaining_amount +
                bs.op_dollars_equivalent_remaining_amount + bs.eth_dollars_equivalent_remaining_amount +
                bs.stark_dollars_equivalent_remaining_amount, 0)          as total_dollars_equivalent_remaining_amount

from project_details pd2
         join contribution_stats cs on cs.project_id = pd2.project_id
         left join reward_stats rs on rs.project_id = pd2.project_id
         left join budget_stats bs on bs.project_id = pd2.project_id
where (EXISTS(select 1
              from project_github_repos pgr2
                       join indexer_exp.github_repos gr2
                            on gr2.id = pgr2.github_repo_id and pgr2.project_id = pd2.project_id and
                               gr2.visibility = 'PUBLIC'));