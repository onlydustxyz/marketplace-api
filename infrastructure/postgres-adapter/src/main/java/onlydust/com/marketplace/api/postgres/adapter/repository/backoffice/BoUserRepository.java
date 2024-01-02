package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoUserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoUserRepository extends JpaRepository<BoUserEntity, UUID> {

    @Query(value = """
            WITH repo_languages AS (SELECT contributor_id, jsonb_agg(KEY) AS languages
                                    FROM (SELECT contributor_id,
                                                 jsonb_object_keys(languages)                             AS KEY,
                                                 CAST((languages -> jsonb_object_keys(languages)) AS int) AS value
                                          FROM (SELECT contributor_id, jsonb_concat_agg(languages) languages
                                                FROM indexer_exp.contributions c
                                                         JOIN indexer_exp.github_repos r ON r.id = c.repo_id
                                                GROUP BY contributor_id) repo_languages_stats
                                          ORDER BY value DESC) repo_languages
                                    group by contributor_id),
                 user_languages AS (SELECT user_id, jsonb_agg(KEY) AS languages
                                    FROM (SELECT id                                                       as user_id,
                                                 jsonb_object_keys(languages)                             AS KEY,
                                                 CAST((languages -> jsonb_object_keys(languages)) AS int) AS value
                                          FROM user_profile_info
                                          ORDER BY value DESC) user_languages_stats
                                    group by user_id)
            SELECT u.id                                                AS id,
                   u.created_at                                        AS created_at,
                   GREATEST(upay.tech_updated_at, upi.tech_updated_at, u.created_at) AS updated_at,
                   u.last_seen_at                                      AS last_seen_at,
                   upay.identity #> '{Company}' IS NOT NULL            AS is_company,
                   upay.identity #>> '{Company,name}'                  AS company_name,
                   upay.identity #>> '{Company,identification_number}' AS company_num,
                   COALESCE(upay.identity #>> '{Company,owner,firstname}', upay.identity #>> '{Person,firstname}') AS firstname,
                   COALESCE(upay.identity #>> '{Company,owner,lastname}', upay.identity #>> '{Person,lastname}')   AS lastname,
                   upay.location #>> '{address}'                       AS address,
                   upay.location #>> '{post_code}'                     AS post_code,
                   upay.location #>> '{city}'                          AS city,
                   upay.location #>> '{country}'                       AS country,
                   ba.bic                                              AS bic,
                   ba.iban                                             AS iban,
                   eth_name.address                                    AS ens,
                   eth_address.address                                 AS eth_address,
                   aptos_address.address                               AS aptos_address,
                   optimism_wallet.address                             AS optimism_address,
                   starknet_address.address                            AS starknet_address,
                   u.github_user_id                                    AS github_user_id,
                   COALESCE(gu.login, u.github_login)                  AS github_login,
                   COALESCE(gu.html_url, 'https://github.com/' || u.github_login) AS github_html_url,
                   COALESCE(gu.avatar_url, u.github_avatar_url)        AS github_avatar_url,
                   COALESCE(telegram.contact, gu.telegram)             AS telegram,
                   COALESCE(twitter.contact, gu.twitter)               AS twitter,
                   discord.contact                                     AS discord,
                   COALESCE(linkedin.contact, gu.linkedin)             AS linkedin,
                   COALESCE(email.contact, u.email)                    AS email,
                   whatsapp.contact                                    AS whatsapp,
                   COALESCE(upi.bio, gu.bio)                           AS bio,
                   COALESCE(upi.location, gu.location)                 AS location,
                   COALESCE(upi.website, gu.website)                   AS website,
                   upi.looking_for_a_job                               AS looking_for_a_job,
                   upi.weekly_allocated_time                           AS weekly_allocated_time,
                   COALESCE(ul.languages, rl.languages)                AS languages,
                   o.terms_and_conditions_acceptance_date              AS tc_accepted_at,
                   o.profile_wizard_display_date                       AS onboarding_completed_at
            FROM iam.users u
                     LEFT JOIN indexer_exp.github_accounts gu ON gu.id = u.github_user_id
                     LEFT JOIN user_profile_info upi ON upi.id = u.id
                     LEFT JOIN user_payout_info upay ON upay.user_id = u.id
                     LEFT JOIN bank_accounts ba ON u.id = ba.user_id
                     LEFT JOIN wallets eth_name
                               ON eth_name.user_id = u.id AND eth_name.network = 'ethereum' AND eth_name.type = 'name'
                     LEFT JOIN wallets eth_address
                               ON eth_address.user_id = u.id AND eth_address.network = 'ethereum' AND eth_address.type = 'address'
                     LEFT JOIN wallets aptos_address ON aptos_address.user_id = u.id AND aptos_address.network = 'aptos' AND
                                                        aptos_address.type = 'address'
                     LEFT JOIN wallets optimism_wallet
                               ON optimism_wallet.user_id = u.id AND optimism_wallet.network = 'optimism' AND
                                  optimism_wallet.type = 'address'
                     LEFT JOIN wallets starknet_address
                               ON starknet_address.user_id = u.id AND starknet_address.network = 'starknet' AND
                                  starknet_address.type = 'address'
                     LEFT JOIN contact_informations telegram ON telegram.user_id = u.id AND telegram.channel = 'telegram'
                     LEFT JOIN contact_informations twitter ON twitter.user_id = u.id AND twitter.channel = 'twitter'
                     LEFT JOIN contact_informations linkedin ON linkedin.user_id = u.id AND linkedin.channel = 'linkedin'
                     LEFT JOIN contact_informations email ON email.user_id = u.id AND email.channel = 'email'
                     LEFT JOIN contact_informations discord ON discord.user_id = u.id AND discord.channel = 'discord'
                     LEFT JOIN contact_informations whatsapp ON whatsapp.user_id = u.id AND whatsapp.channel = 'whatsapp'
                     LEFT JOIN onboardings o ON u.id = o.user_id
                     LEFT JOIN repo_languages rl ON rl.contributor_id = u.github_user_id
                     LEFT JOIN user_languages ul ON ul.user_id = u.id
                WHERE
                    COALESCE(:userIds) IS NULL OR u.id IN (:userIds)
            """,
            countQuery = "SELECT count(*) FROM iam.users WHERE COALESCE(:userIds) IS NULL OR id IN (:userIds)",
            nativeQuery = true)
    @NotNull
    Page<BoUserEntity> findAll(final List<UUID> userIds, final @NotNull Pageable pageable);
}
