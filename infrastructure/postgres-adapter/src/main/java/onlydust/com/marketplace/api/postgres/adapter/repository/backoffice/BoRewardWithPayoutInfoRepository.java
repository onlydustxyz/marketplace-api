package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoRewardWithPayoutInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoRewardWithPayoutInfoRepository extends JpaRepository<BoRewardWithPayoutInfoEntity, UUID> {

    @Query(value = """
            SELECT r
            FROM  BoRewardWithPayoutInfoEntity r
            WHERE r.invoice.id IN :invoiceIds
            """)
    List<BoRewardWithPayoutInfoEntity> findByInvoiceIds(@NonNull List<UUID> invoiceIds);
}
