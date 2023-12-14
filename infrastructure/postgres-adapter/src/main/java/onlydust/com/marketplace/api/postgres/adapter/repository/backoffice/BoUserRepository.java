package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoPaymentEntity;
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
                                                         JOIN github_repos r ON r.id = c.repo_id
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
            SELECT au.id                                               AS id,
                   au.created_at                                       AS created_at,
                   GREATEST(upay.tech_updated_at, upi.tech_updated_at, au.created_at) AS updated_at,
                   au.last_seen                                        AS last_seen_at,
                   upay.identity #>> '{Company,name}'                  AS company_name,
                   upay.identity #>> '{Company,identification_number}' AS company_num,
                   upay.identity #>> '{Company,owner,firstname}'       AS company_firstname,
                   upay.identity #>> '{Company,owner,lastname}'        AS company_lastname,
                   upay.identity #>> '{Person,firstname}'              AS person_firstname,
                   upay.identity #>> '{Person,lastname}'               AS person_lastname,
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
                   gu.id                                               AS github_user_id,
                   gu.login                                            AS github_login,
                   gu.html_url                                         AS github_html_url,
                   gu.avatar_url                                       AS github_avatar_url,
                   COALESCE(telegram.contact, gu.telegram)             AS telegram,
                   COALESCE(twitter.contact, gu.twitter)               AS twitter,
                   discord.contact                                     AS discord,
                   COALESCE(linkedin.contact, gu.linkedin)             AS linkedin,
                   COALESCE(email.contact, au.email)                   AS email,
                   whatsapp.contact                                    AS whatsapp,
                   COALESCE(upi.bio, gu.bio)                           AS bio,
                   COALESCE(upi.location, gu.location)                 AS location,
                   COALESCE(upi.website, gu.website)                   AS website,
                   upi.looking_for_a_job                               AS looking_for_a_job,
                   upi.weekly_allocated_time                           AS weekly_allocated_time,
                   COALESCE(ul.languages, rl.languages)                AS languages,
                   o.terms_and_conditions_acceptance_date              AS tc_accepted_at,
                   o.profile_wizard_display_date                       AS onboarding_completed_at
            FROM auth_users au
                     LEFT JOIN indexer_exp.github_accounts gu ON gu.id = au.github_user_id
                     LEFT JOIN user_profile_info upi ON upi.id = au.id
                     LEFT JOIN user_payout_info upay ON upay.user_id = au.id
                     LEFT JOIN bank_accounts ba ON au.id = ba.user_id
                     LEFT JOIN wallets eth_name
                               ON eth_name.user_id = au.id AND eth_name.network = 'ethereum' AND eth_name.type = 'name'
                     LEFT JOIN wallets eth_address
                               ON eth_address.user_id = au.id AND eth_address.network = 'ethereum' AND eth_address.type = 'address'
                     LEFT JOIN wallets aptos_address ON aptos_address.user_id = au.id AND aptos_address.network = 'aptos' AND
                                                        aptos_address.type = 'address'
                     LEFT JOIN wallets optimism_wallet
                               ON optimism_wallet.user_id = au.id AND optimism_wallet.network = 'optimism' AND
                                  optimism_wallet.type = 'address'
                     LEFT JOIN wallets starknet_address
                               ON starknet_address.user_id = au.id AND starknet_address.network = 'starknet' AND
                                  starknet_address.type = 'address'
                     LEFT JOIN contact_informations telegram ON telegram.user_id = au.id AND telegram.channel = 'telegram'
                     LEFT JOIN contact_informations twitter ON twitter.user_id = au.id AND twitter.channel = 'twitter'
                     LEFT JOIN contact_informations linkedin ON linkedin.user_id = au.id AND linkedin.channel = 'linkedin'
                     LEFT JOIN contact_informations email ON email.user_id = au.id AND email.channel = 'email'
                     LEFT JOIN contact_informations discord ON discord.user_id = au.id AND discord.channel = 'discord'
                     LEFT JOIN contact_informations whatsapp ON whatsapp.user_id = au.id AND whatsapp.channel = 'whatsapp'
                     LEFT JOIN onboardings o ON au.id = o.user_id
                     LEFT JOIN repo_languages rl ON rl.contributor_id = au.github_user_id
                     LEFT JOIN user_languages ul ON ul.user_id = au.id
                WHERE 
                    COALESCE(:userIds) IS NULL OR au.id IN (:userIds)
            """,
            countQuery = "SELECT count(*) FROM auth_users WHERE COALESCE(:userIds) IS NULL OR id IN (:userIds)", nativeQuery = true)
    @NotNull
    Page<BoUserEntity> findAll(final List<UUID> userIds, final @NotNull Pageable pageable);
}
