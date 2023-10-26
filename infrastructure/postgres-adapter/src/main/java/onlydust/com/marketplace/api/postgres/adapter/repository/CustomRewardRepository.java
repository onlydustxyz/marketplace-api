package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.UUID;

@AllArgsConstructor
public class CustomRewardRepository {
    private final EntityManager entityManager;

    private static final String FIND_REWARD_BY_ID = """
            select pr.requested_at,
                   r.processed_at,
                   gu_recipient.login                                                                       recipient_login,
                   gu_recipient.avatar_url                                                                  recipient_avatar_url,
                   gu_recipient.id                                                                          recipient_id,
                   gu_requestor.login                                                                       requestor_login,
                   gu_requestor.avatar_url                                                                  requestor_avatar_url,
                   gu_requestor.id                                                                          requestor_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else cuq.price * pr.amount end dollars_equivalent,
                   case
                       when au.id is null then 'PENDING_SIGNUP'
                       when r.id is not null then 'COMPLETE'
                       else 'PROCESSING'
                       end                                                                                  status
            from payment_requests pr
                     join github_users gu_recipient on gu_recipient.id = pr.recipient_id
                     left join public.auth_users au on gu_recipient.id = au.github_user_id
                     join auth_users au_requestor on au_requestor.id = pr.requestor_id
                     join github_users gu_requestor on gu_requestor.id = au_requestor.github_user_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     where pr.id = :rewardId""";

    public RewardViewEntity findProjectRewardViewEntityByd(final UUID rewardId) {
        try {
            return (RewardViewEntity) entityManager.createNativeQuery(FIND_REWARD_BY_ID, RewardViewEntity.class)
                    .setParameter("rewardId", rewardId)
                    .getSingleResult();
        } catch (NoResultException noResultException) {
            throw OnlyDustException.notFound("Reward not found", noResultException);
        }
    }
}
