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
            WITH repo_languages AS (SELECT contributor_id, jsonb_agg(DISTINCT grl.language) AS languages
                                     FROM indexer_exp.contributions c
                                              JOIN indexer_exp.github_repo_languages grl ON grl.repo_id = c.repo_id
                                     GROUP BY contributor_id),
                  user_languages AS (SELECT user_id, jsonb_agg(KEY) AS languages
                                     FROM (SELECT id                                                       as user_id,
                                                  jsonb_object_keys(languages)                             AS KEY,
                                                  CAST((languages -> jsonb_object_keys(languages)) AS int) AS value
                                           FROM user_profile_info
                                           ORDER BY value DESC) user_languages_stats
                                     group by user_id)
             SELECT u.id                                                                                                      AS id,
                    u.created_at                                                                                              AS created_at,
                    GREATEST(bp.tech_updated_at, kyc.tech_updated_at, kyb.tech_updated_at, upi.tech_updated_at, u.created_at) AS updated_at,
                    u.last_seen_at                                                                                            AS last_seen_at,
                    coalesce(bp.type != 'INDIVIDUAL', false)                                                                  AS is_company,
                    coalesce(bp.verification_status, 'NOT_STARTED')                                                           AS verification_status,
                    kyb.name                                                                                                  AS company_name,
                    kyb.registration_number                                                                                   AS company_num,
                    kyc.first_name                                                                                            AS firstname,
                    kyc.last_name                                                                                             AS lastname,
                    coalesce(kyc.address, kyb.address)                                                                        AS address,
                    coalesce(kyc.country, kyb.country)                                                                        AS country,
                    coalesce(kyc.us_citizen, kyb.us_entity)                                                                   AS us_entity,
                    ba.bic                                                                                                    AS bic,
                    ba.number                                                                                                 AS iban,
                    eth_name.address                                                                                          AS ens,
                    eth_address.address                                                                                       AS eth_address,
                    aptos_address.address                                                                                     AS aptos_address,
                    optimism_wallet.address                                                                                   AS optimism_address,
                    starknet_address.address                                                                                  AS starknet_address,
                    u.github_user_id                                                                                          AS github_user_id,
                    COALESCE(gu.login, u.github_login)                                                                        AS github_login,
                    COALESCE(gu.html_url, 'https://github.com/' || u.github_login)                                            AS github_html_url,
                    COALESCE(gu.avatar_url, u.github_avatar_url)                                                              AS github_avatar_url,
                    COALESCE(telegram.contact, gu.telegram)                                                                   AS telegram,
                    COALESCE(twitter.contact, gu.twitter)                                                                     AS twitter,
                    discord.contact                                                                                           AS discord,
                    COALESCE(linkedin.contact, gu.linkedin)                                                                   AS linkedin,
                    u.email                                                                                                   AS email,
                    whatsapp.contact                                                                                          AS whatsapp,
                    COALESCE(upi.bio, gu.bio)                                                                                 AS bio,
                    COALESCE(upi.location, gu.location)                                                                       AS location,
                    COALESCE(upi.website, gu.website)                                                                         AS website,
                    upi.looking_for_a_job                                                                                     AS looking_for_a_job,
                    upi.weekly_allocated_time                                                                                 AS weekly_allocated_time,
                    COALESCE(ul.languages, rl.languages)                                                                      AS languages,
                    o.terms_and_conditions_acceptance_date                                                                    AS tc_accepted_at,
                    o.profile_wizard_display_date                                                                             AS onboarding_completed_at
             FROM iam.users u
                      LEFT JOIN indexer_exp.github_accounts gu ON gu.id = u.github_user_id
                      LEFT JOIN user_profile_info upi on upi.id = u.id
                      LEFT JOIN LATERAL (
                        SELECT bp.* from accounting.billing_profiles bp
                        JOIN accounting.billing_profiles_users bpu ON bpu.billing_profile_id = bp.id
                        WHERE bpu.user_id = u.id
                        LIMIT 1
                      ) bp ON true
                      LEFT JOIN accounting.kyc ON kyc.billing_profile_id = bp.id
                      LEFT JOIN accounting.kyb ON kyb.billing_profile_id = bp.id
                      LEFT JOIN accounting.bank_accounts ba ON bp.id = ba.billing_profile_id
                      LEFT JOIN accounting.wallets eth_name
                                ON eth_name.billing_profile_id = bp.id AND eth_name.network = 'ethereum' AND eth_name.type = 'name'
                      LEFT JOIN accounting.wallets eth_address
                                ON eth_address.billing_profile_id = bp.id AND eth_address.network = 'ethereum' AND eth_address.type = 'address'
                      LEFT JOIN accounting.wallets aptos_address ON aptos_address.billing_profile_id = u.id AND aptos_address.network = 'aptos' AND
                                                         aptos_address.type = 'address'
                      LEFT JOIN accounting.wallets optimism_wallet
                                ON optimism_wallet.billing_profile_id = bp.id AND optimism_wallet.network = 'optimism' AND
                                   optimism_wallet.type = 'address'
                      LEFT JOIN accounting.wallets starknet_address
                                ON starknet_address.billing_profile_id = bp.id AND starknet_address.network = 'starknet' AND
                                   starknet_address.type = 'address'
                      LEFT JOIN contact_informations telegram ON telegram.user_id = u.id AND telegram.channel = 'telegram'
                      LEFT JOIN contact_informations twitter ON twitter.user_id = u.id AND twitter.channel = 'twitter'
                      LEFT JOIN contact_informations linkedin ON linkedin.user_id = u.id AND linkedin.channel = 'linkedin'
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
