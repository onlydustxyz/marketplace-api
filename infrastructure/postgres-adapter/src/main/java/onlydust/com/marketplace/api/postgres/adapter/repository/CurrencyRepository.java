package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, UUID> {
    Boolean existsByCode(String code);

    Optional<CurrencyEntity> findByCode(String code);

    @Query(value = """
            SELECT c.*
            FROM currencies c
            JOIN rewards r on r.currency_id = c.id
            WHERE r.recipient_id = :githubUserId
                OR
                (coalesce(:administratedBillingProfileIds) IS NOT NULL AND r.billing_profile_id IN (:administratedBillingProfileIds))
            ORDER BY c.code
            """, nativeQuery = true)
    Set<CurrencyEntity> listUserRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds);

    @Query("""
            SELECT c
            FROM CurrencyEntity c
            JOIN FETCH c.erc20 e
            WHERE e.blockchain = :blockchain AND LOWER(e.address) = LOWER(:contractAddress)
            """)
    Optional<CurrencyEntity> findByErc20(NetworkEnumEntity blockchain, String contractAddress);
}
