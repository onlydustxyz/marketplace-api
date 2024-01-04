package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.UUID;
import javax.persistence.EntityManager;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;

@AllArgsConstructor
public class CustomUserPayoutInfoRepository {

  private final EntityManager entityManager;

  private static final String FIND_USER_PAYOUT_INFO_VALIDATIONS = """
      select u.id                                 user_id,
             coalesce(currencies.list, '{}')      payment_requests_currencies
      from iam.users u
           left join (select pr.recipient_id, array_agg(distinct pr.currency) as list
                     from payment_requests pr
                     left join payments p on p.request_id = pr.id
                     where p is null
                     group by pr.recipient_id) currencies on currencies.recipient_id = u.github_user_id
      where u.id = :userId
      """;

  public UserPayoutInfoValidationEntity getUserPayoutInfoValidationEntity(final UUID userId) {
    return (UserPayoutInfoValidationEntity) entityManager.createNativeQuery(FIND_USER_PAYOUT_INFO_VALIDATIONS,
            UserPayoutInfoValidationEntity.class)
        .setParameter("userId", userId)
        .getSingleResult();
  }
}
