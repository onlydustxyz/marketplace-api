package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class CustomUserPayoutInfoRepository {
    private final EntityManager entityManager;

    private static final String FIND_USER_PAYOUT_INFO_VALIDATIONS = """
            select au.id user_id,
                   (select count(pr.id) > 0
                    from payment_requests pr
                             left join payments p on p.request_id = pr.id
                    where p.id is null
                      and pr.recipient_id = au.github_user_id)                                       has_pending_payments,
                   (upi.identity is not null and upi.identity -> 'Person' is not null and
                   upi.identity -> 'Person' -> 'lastname' != cast('null' as jsonb) and
                   upi.identity -> 'Person' -> 'firstname' != cast('null' as jsonb))            valid_person,
                  (upi.location is not null and upi.location -> 'city' != cast('null' as jsonb) and
                   upi.location -> 'post_code' != cast('null' as jsonb) and
                   upi.location -> 'address' != cast('null' as jsonb) and
                   upi.location -> 'country' != cast('null' as jsonb))                          valid_location,
                  (upi.identity is not null and upi.identity -> 'Company' is not null and
                   upi.identity -> 'Company' -> 'name' != cast('null' as jsonb) and
                   upi.identity -> 'Company' -> 'identification_number' != cast('null' as jsonb) and
                   upi.identity -> 'Company' -> 'owner' is not null and
                   upi.identity -> 'Company' -> 'owner' -> 'firstname' != cast('null' as jsonb) and
                   upi.identity -> 'Company' -> 'owner' -> 'lastname' != cast('null' as jsonb)) valid_company,
                   coalesce((select w_op.address is not null
                             from payment_requests pr_op
                                      left join payments p_op on p_op.request_id = pr_op.id
                                      left join wallets w_op on w_op.user_id = upi.user_id and w_op.network = 'optimism'
                             where pr_op.currency = 'op'
                               and pr_op.recipient_id = au.github_user_id
                               and p_op is null
                             limit 1), true)                                                         valid_op_wallet,
                   coalesce((select w_stark.address is not null
                             from payment_requests pr_stark
                                      left join payments p_stark on p_stark.request_id = pr_stark.id
                                      left join wallets w_stark on w_stark.user_id = upi.user_id and w_stark.network = 'starknet'
                             where pr_stark.currency = 'stark'
                               and pr_stark.recipient_id = au.github_user_id
                               and p_stark is null
                             limit 1), true)                                                         valid_stark_wallet,
                   coalesce((select w_apt.address is not null
                             from payment_requests pr_apt
                                      left join payments p_apt on p_apt.request_id = pr_apt.id
                                      left join wallets w_apt on w_apt.user_id = upi.user_id and w_apt.network = 'aptos'
                             where pr_apt.currency = 'apt'
                               and pr_apt.recipient_id = au.github_user_id
                               and p_apt is null
                             limit 1), true)                                                         valid_apt_wallet,
                   case
                       when (
                           (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'fiat' and
                            (select count(pr_usd.id) > 0
                             from payment_requests pr_usd
                                      left join payments p_usd on p_usd.request_id = pr_usd.id
                             where pr_usd.recipient_id = au.github_user_id
                               and pr_usd.currency = 'usd'
                               and p_usd.id is null))
                           ) then (select count(*) > 0
                                   from bank_accounts ba
                                   where ba.user_id = upi.user_id)
                       else true end                                                                 valid_banking_account,
                   case
                       when (upi.identity -> 'Person' is not null) then (
                           coalesce((select w_eth.address is not null
                                     from payment_requests pr_usdc
                                              left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                              left join wallets w_eth on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                     where pr_usdc.currency = 'usd'
                                       and pr_usdc.recipient_id = au.github_user_id
                                       and p_usdc is null
                                     limit 1), true)
                           )
                       when (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'crypto') then (
                           coalesce((select w_eth.address is not null
                                     from payment_requests pr_usdc
                                              left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                              left join wallets w_eth
                                                        on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                     where pr_usdc.currency = 'usd'
                                       and pr_usdc.recipient_id = au.github_user_id
                                       and p_usdc is null
                                     limit 1), true)
                           )
                       else true
                       end                                                                           valid_usdc_wallet,
                   coalesce((select w_eth.address is not null
                             from payment_requests pr_eth
                                      left join payments p_eth on p_eth.request_id = pr_eth.id
                                      left join wallets w_eth on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                             where pr_eth.currency = 'eth'
                               and pr_eth.recipient_id = au.github_user_id
                               and p_eth is null
                             limit 1), true)                                                         valid_eth_wallet
            from auth_users au
                     left join user_payout_info upi on upi.user_id = au.id
            where au.id = :userId""";

    public Optional<UserPayoutInfoValidationEntity> getUserPayoutInfoValidationEntity(final UUID userId) {
        try {
            return Optional.of((UserPayoutInfoValidationEntity) entityManager.createNativeQuery(FIND_USER_PAYOUT_INFO_VALIDATIONS,
                            UserPayoutInfoValidationEntity.class)
                    .setParameter("userId", userId)
                    .getSingleResult());
        } catch (NoResultException noResultException) {
            return Optional.empty();
        }
    }
}
