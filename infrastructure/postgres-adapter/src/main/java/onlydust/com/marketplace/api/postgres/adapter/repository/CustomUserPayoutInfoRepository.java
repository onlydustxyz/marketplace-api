package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;

import javax.persistence.EntityManager;
import java.util.UUID;

@AllArgsConstructor
public class CustomUserPayoutInfoRepository {
    private final EntityManager entityManager;

    private static final String FIND_USER_PAYOUT_INFO_VALIDATIONS = """
            select upi.user_id,
                   (upi.identity is not null and upi.identity -> 'Person' is not null and
                    upi.identity -> 'Person' -> 'lastname' is not null and
                    upi.identity -> 'Person' -> 'firstname' is not null)                             valid_person,
                   (upi.location is not null and upi.location -> 'city' is not null and upi.location -> 'post_code' is not null and
                    upi.location -> 'address' is not null and upi.location -> 'country' is not null) valid_location,
                   (upi.identity is not null and upi.identity -> 'Company' is not null and
                    upi.identity -> 'Company' -> 'name' is not null and
                    upi.identity -> 'Company' -> 'identification_number' is not null and
                    upi.identity -> 'Company' -> 'owner' is not null and
                    upi.identity -> 'Company' -> 'owner' -> 'firstname' is not null and
                    upi.identity -> 'Company' -> 'owner' -> 'lastname' is not null)                  valid_company,
                   coalesce((select w_op.address is not null
                             from payment_requests pr_op
                                      left join wallets w_op on w_op.user_id = upi.user_id and w_op.network = 'optimism'
                             where pr_op.currency = 'op'
                               and pr_op.recipient_id = au.github_user_id
                             limit 1), true)                                                         valid_op_wallet,
                   coalesce((select w_stark.address is not null
                             from payment_requests pr_stark
                                      left join wallets w_stark on w_stark.user_id = upi.user_id and w_stark.network = 'starknet'
                             where pr_stark.currency = 'stark'
                               and pr_stark.recipient_id = au.github_user_id
                             limit 1), true)                                                         valid_stark_wallet,
                   coalesce((select w_apt.address is not null
                             from payment_requests pr_apt
                                      left join wallets w_apt on w_apt.user_id = upi.user_id and w_apt.network = 'aptos'
                             where pr_apt.currency = 'apt'
                               and pr_apt.recipient_id = au.github_user_id
                             limit 1), true)                                                         valid_apt_wallet,
                   coalesce((select w_eth.address is not null
                             from payment_requests pr_eth
                                      left join wallets w_eth on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                             where pr_eth.currency = 'eth'
                               and pr_eth.recipient_id = au.github_user_id
                             limit 1), true)                                                         valid_eth_wallet,
                   coalesce((select ba.bic is not null and ba.iban is not null
                             from payment_requests pr_usd
                                      left join bank_accounts ba on ba.user_id = upi.user_id
                             where pr_usd.recipient_id = au.github_user_id
                               and pr_usd.currency = 'usd'
                             limit 1), true)                                                         valid_banking_account
            from user_payout_info upi
                     join auth_users au on upi.user_id = au.id
            where upi.user_id = :userId""";

    public UserPayoutInfoValidationEntity getUserPayoutInfoValidationEntity(final UUID userId) {
        return (UserPayoutInfoValidationEntity) entityManager.createNativeQuery(FIND_USER_PAYOUT_INFO_VALIDATIONS,
                        UserPayoutInfoValidationEntity.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
