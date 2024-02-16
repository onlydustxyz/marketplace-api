package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    void deleteAllByBillingProfileIdAndStatus(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status status);

    Page<InvoiceEntity> findAllByBillingProfileId(final @NonNull UUID billingProfileId, final @NonNull Pageable pageable);
}
