package onlydust.com.marketplace.api.postgres.adapter.repository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class CustomProjectRankingRepository {

    private final EntityManager entityManager;

    public void updateProjectsRanking() {
        final int executeUpdate = entityManager.createNativeQuery(UPDATE_PROJECT_RANKING_QUERY)
                .executeUpdate();
        LOGGER.info("{} project's rank(s) were updated", executeUpdate);
    }

    private static final String UPDATE_PROJECT_RANKING_QUERY = """
            with subquery as (with s AS (SELECT AVG(psfrc.pr_count_last_3_months)                         AS avg_volume_of_pull_requests_last_3_months,
                                                STDDEV(psfrc.pr_count_last_3_months)                      AS stddev_volume_of_pull_requests_last_3_months,
                                                AVG(psfrc.contributor_count)                              AS avg_number_of_distinct_contributors,
                                                STDDEV(psfrc.contributor_count)                           AS stddev_number_of_distinct_contributors,
                                                AVG(psfrc.open_issue_count)                               AS avg_number_of_open_issues,
                                                STDDEV(psfrc.open_issue_count)                            AS stddev_number_of_open_issues,
                                                AVG(psfrc.total_dollars_equivalent_spent_last_1_month)    AS avg_total_grants_last_1_month,
                                                STDDEV(psfrc.total_dollars_equivalent_spent_last_1_month) AS stddev_total_grants_last_1_month,
                                                AVG(psfrc.distinct_recipient_number_last_1_months)        AS avg_number_of_distinct_recipients_last_1_month,
                                                STDDEV(psfrc.distinct_recipient_number_last_1_months)     AS stddev_number_of_distinct_recipients_last_1_month,
                                                AVG(psfrc.total_dollars_equivalent_remaining_amount)      AS avg_amount_remaining_to_distribute,
                                                STDDEV(psfrc.total_dollars_equivalent_remaining_amount)   AS stddev_amount_remaining_to_distribute
                                         FROM project_stats_for_ranking_computation psfrc),
                                   project_date_stats AS ((SELECT p.id as project_id,
                                                                  GREATEST(
                                                                          ROUND(
                                                                                          ((CURRENT_DATE - cast(p.created_at as date)) - pas.avg_days_since_creation) /
                                                                                          NULLIF(pas.stddev_days_since_creation, 0) *
                                                                                          -1,
                                                                                          2
                                                                          ),
                                                                          0
                                                                  ) AS normalized_inverted_days_since_creation
                                                           FROM projects p,
                                                                (SELECT AVG(CURRENT_DATE - cast(p.created_at as date))    AS avg_days_since_creation,
                                                                        STDDEV(CURRENT_DATE - cast(p.created_at as date)) AS stddev_days_since_creation
                                                                 FROM projects) pas))
                              select p.id as project_id,
                                     (normalized_values.normalized_amount_remaining_to_distribute +
                                      normalized_values.normalized_creation_date +
                                      normalized_values.normalized_number_of_distinct_contributors +
                                      normalized_values.normalized_number_of_distinct_recipients +
                                      normalized_values.normalized_number_of_open_issues +
                                      normalized_values.normalized_total_grants +
                                      normalized_values.normalized_volume_of_pull_requests
                                         ) * 100 AS total_normalized_score
                              from projects p
                                       join (select project_stats.project_id,
                                                    LEAST((project_stats.pr_count - s.avg_volume_of_pull_requests_last_3_months) /
                                                          NULLIF(s.stddev_volume_of_pull_requests_last_3_months, 0),
                                                          3)                                        AS normalized_volume_of_pull_requests,
                                                    LEAST((project_stats.distinct_recipient_number_last_1_months -
                                                           s.avg_number_of_distinct_contributors) /
                                                          NULLIF(s.stddev_number_of_distinct_contributors, 0),
                                                          3)                                        AS normalized_number_of_distinct_contributors,
                                                    LEAST((project_stats.open_issue_count - s.avg_number_of_open_issues) /
                                                          NULLIF(s.stddev_number_of_open_issues, 0),
                                                          3)                                        AS normalized_number_of_open_issues,
                                                    LEAST((project_stats.total_dollars_equivalent_spent_last_1_month -
                                                           s.avg_total_grants_last_1_month) /
                                                          NULLIF(s.stddev_total_grants_last_1_month, 0), 3) *
                                                    2                                               AS normalized_total_grants,
                                                    LEAST((project_stats.distinct_recipient_number_last_1_months -
                                                           s.avg_number_of_distinct_recipients_last_1_month) /
                                                          NULLIF(s.stddev_number_of_distinct_recipients_last_1_month, 0), 3) *
                                                    2                                               AS normalized_number_of_distinct_recipients,
                                                    LEAST((project_stats.total_dollars_equivalent_remaining_amount -
                                                           s.avg_amount_remaining_to_distribute) /
                                                          NULLIF(s.stddev_amount_remaining_to_distribute, 0), 3) *
                                                    2                                               AS normalized_amount_remaining_to_distribute,
                                                    pds.normalized_inverted_days_since_creation * 4 AS normalized_creation_date
                                             from project_stats_for_ranking_computation project_stats
                                                      cross join s
                                                      join project_date_stats pds on pds.project_id = project_stats.project_id) normalized_values
                                            on normalized_values.project_id = p.id)
            update projects p_to_update
            set rank = subquery.total_normalized_score
            from subquery
            where p_to_update.id = subquery.project_id""";
}
