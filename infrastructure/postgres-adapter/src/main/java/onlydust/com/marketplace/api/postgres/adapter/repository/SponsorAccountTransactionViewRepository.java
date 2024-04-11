package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountTransactionViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SponsorAccountTransactionViewRepository extends JpaRepository<SponsorAccountTransactionViewEntity, UUID> {
    Page<SponsorAccountTransactionViewEntity> findAllBySponsorAccountSponsorIdAndTypeIn(UUID sponsorId,
                                                                                        List<SponsorAccountTransactionViewEntity.TransactionType> types,
                                                                                        Pageable pageable);
}
