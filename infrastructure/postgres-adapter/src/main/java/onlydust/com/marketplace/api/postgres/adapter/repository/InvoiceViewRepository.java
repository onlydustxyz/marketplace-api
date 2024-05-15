package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.InvoiceViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceViewRepository extends JpaRepository<InvoiceViewEntity, UUID> {
    List<InvoiceViewEntity> findAllByBillingProfileIdAndStatus(final @NonNull UUID billingProfileId, final @NonNull Invoice.Status status);

    Page<InvoiceViewEntity> findAllByBillingProfileIdAndStatusNot(final @NonNull UUID billingProfileId, final @NonNull Invoice.Status exceptStatus,
                                                                  final @NonNull Pageable pageable);
}
