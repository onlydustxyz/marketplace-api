package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CustomUserRewardRepository {

    private final EntityManager entityManager;

    private static final String COUNT_USER_REWARDS = """
            select count(distinct pr.id)
            from auth_users au
                     join payment_requests pr on pr.recipient_id = au.github_user_id and au.id = :userId
            """;

    protected static final String FIND_USER_REWARDS_BY_ID = """
            select pr.requested_at,
                   pd.name,
                   pd.logo_url,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else coalesce(cuq.price, 0) * pr.amount end dollars_equivalent,
                   case
                       when pr.invoice_received_at is null then 'PENDING_INVOICE'
                       when r.id is not null then 'COMPLETE'
                       else 'PROCESSING'
                       end                                                           status
            from auth_users au
                     join payment_requests pr on pr.recipient_id = au.github_user_id and au.id = :userId
                     join project_details pd on pd.project_id = pr.project_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     order by %order_by% offset :offset limit :limit
                     """;

    public Integer getCount(UUID userId) {
        final var query = entityManager
                .createNativeQuery(COUNT_USER_REWARDS)
                .setParameter("userId", userId);
        return ((Number) query.getSingleResult()).intValue();
    }

    public List<UserRewardViewEntity> getViewEntities(UUID userId, UserRewardView.SortBy sortBy,
                                                      SortDirection sortDirection, int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, sortDirection), UserRewardViewEntity.class)
                .setParameter("userId", userId)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList();
    }

    protected static String buildQuery(UserRewardView.SortBy sortBy, final SortDirection sortDirection) {
        sortBy = isNull(sortBy) ? UserRewardView.SortBy.requestedAt : sortBy;
        final String sort = switch (sortBy) {
            case amount -> "dollars_equivalent " + sortDirection.name() + ", requested_at desc";
            case contribution -> "contribution_count " + sortDirection.name() + ", requested_at desc";
            case status -> "status " + sortDirection.name() + ", requested_at desc";
            default -> "requested_at " + sortDirection.name();
        };
        return FIND_USER_REWARDS_BY_ID.replace("%order_by%", sort);
    }
}
