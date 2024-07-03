CREATE OR REPLACE VIEW united_stats_per_language_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.contributor_id) as github_user_id,
       l.id                                                             as language_id,
       coalesce(c.contribution_count, 0)                                as contribution_count,
       coalesce(r.reward_count, 0)                                      as reward_count,
       coalesce(r.pending_request_reward_count, 0)                      as pending_request_reward_count,
       coalesce(r.usd_total, 0)                                         as usd_total,
       coalesce(r.max_usd, 0)                                           as max_usd,
       ranks.rank                                                       as rank,
       l.name                                                           as language_name
FROM contributions_stats_per_language_per_user c
         FULL JOIN received_rewards_stats_per_language_per_user r
                   ON r.recipient_id = c.contributor_id AND r.language_id = c.language_id
         FULL JOIN users_languages_ranks ranks
                   ON ranks.contributor_id = coalesce(c.contributor_id, r.recipient_id) AND
                      ranks.language_id = coalesce(c.language_id, r.language_id)
         JOIN languages l ON l.id = coalesce(c.language_id, r.language_id, ranks.language_id);



CREATE OR REPLACE VIEW united_stats_per_ecosystem_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.contributor_id) as github_user_id,
       e.id                                                             as ecosystem_id,
       coalesce(c.contribution_count, 0)                                as contribution_count,
       coalesce(r.reward_count, 0)                                      as reward_count,
       coalesce(r.pending_request_reward_count, 0)                      as pending_request_reward_count,
       coalesce(r.usd_total, 0)                                         as usd_total,
       coalesce(r.max_usd, 0)                                           as max_usd,
       ranks.rank                                                       as rank,
       e.name                                                           as ecosystem_name
FROM contributions_stats_per_ecosystem_per_user c
         FULL JOIN received_rewards_stats_per_ecosystem_per_user r
                   ON r.recipient_id = c.contributor_id AND r.ecosystem_id = c.ecosystem_id
         FULL JOIN users_ecosystems_ranks ranks
                   ON ranks.contributor_id = coalesce(c.contributor_id, r.recipient_id) AND
                      ranks.ecosystem_id = coalesce(c.ecosystem_id, r.ecosystem_id)
         JOIN ecosystems e ON e.id = coalesce(c.ecosystem_id, r.ecosystem_id, ranks.ecosystem_id);