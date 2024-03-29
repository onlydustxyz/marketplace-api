package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findAllByBillingProfileIdAndStatus(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status status);

    @Query(value = """
            SELECT
                    i.id                    as id,
                    i.billing_profile_id    as billing_profile_id,
                    i.number                as number,
                    i.created_by            as created_by,
                    i.created_at            as created_at,
                    i.status                as status,
                    i.amount                as amount,
                    i.currency_id           as currency_id,
                    i.url                   as url,
                    i.original_file_name    as original_file_name,
                    i.rejection_reason      as rejection_reason,
                    i.data                  as data
            FROM accounting.invoices i
            join accounting.billing_profiles bp on i.billing_profile_id = bp.id
            left join accounting.kyc on kyc.billing_profile_id = bp.id
            left join accounting.kyb on kyb.billing_profile_id = bp.id
            WHERE (coalesce(:invoiceIds) is null or i.id IN (:invoiceIds))
              AND (coalesce(:invoiceStatuses) is null or cast(i.status as text) IN (:invoiceStatuses))
              AND (coalesce(:currencyIds) is null or EXISTS(SELECT 1 from rewards r WHERE r.invoice_id = i.id and r.currency_id IN (:currencyIds)))
              AND (coalesce(:billingProfileTypes) is null or cast(bp.type as text) IN (:billingProfileTypes))
              AND coalesce(kyc.first_name || ' ' || kyc.last_name, kyb.name) ILIKE '%' || :search || '%'
              AND i.status != 'DRAFT'
            """, nativeQuery = true)
    Page<InvoiceEntity> findAllExceptDrafts(final @NonNull List<UUID> invoiceIds,
                                            final @NonNull List<String> invoiceStatuses,
                                            final @NonNull List<UUID> currencyIds,
                                            final @NonNull List<String> billingProfileTypes,
                                            final @NonNull String search,
                                            final @NonNull Pageable pageable);

    Integer countByBillingProfileIdAndStatusNot(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status exceptStatus);
}
