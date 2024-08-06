package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.stream.Streams.nonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(schema = "accounting", name = "payout_infos")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PayoutInfoEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;

    @OneToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    @Builder.Default
    Set<WalletEntity> wallets = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    BankAccountEntity bankAccount;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    private Date updatedAt;

    public PayoutInfo toDomain() {
        return PayoutInfo.builder()
                .bankAccount(nonNull(bankAccount) ? bankAccount.toDomain() : null)
                .ethWallet(wallet(NetworkEnumEntity.ETHEREUM).map(WalletEntity::ethereum).orElse(null))
                .optimismAddress(wallet(NetworkEnumEntity.OPTIMISM).map(WalletEntity::optimism).orElse(null))
                .aptosAddress(wallet(NetworkEnumEntity.APTOS).map(WalletEntity::aptos).orElse(null))
                .starknetAddress(wallet(NetworkEnumEntity.STARKNET).map(WalletEntity::starknet).orElse(null))
                .stellarAccountId(wallet(NetworkEnumEntity.STELLAR).map(WalletEntity::stellar).orElse(null))
                .build();
    }

    private Optional<WalletEntity> wallet(NetworkEnumEntity network) {
        return wallets.stream().filter(w -> w.getNetwork().equals(network)).findFirst();
    }

    public static PayoutInfoEntity fromDomain(final BillingProfile.Id billingProfileId, final PayoutInfo payoutInfo) {
        return PayoutInfoEntity.builder()
                .billingProfileId(billingProfileId.value())
                .bankAccount(payoutInfo.bankAccount().map(b -> BankAccountEntity.of(billingProfileId, b)).orElse(null))
                .wallets(nonNull(
                        payoutInfo.ethWallet().map(w -> WalletEntity.ethereum(billingProfileId, w)).orElse(null),
                        payoutInfo.optimismAddress().map(w -> WalletEntity.optimism(billingProfileId, w)).orElse(null),
                        payoutInfo.aptosAddress().map(w -> WalletEntity.aptos(billingProfileId, w)).orElse(null),
                        payoutInfo.starknetAddress().map(w -> WalletEntity.starknet(billingProfileId, w)).orElse(null),
                        payoutInfo.stellarAccountId().map(w -> WalletEntity.stellar(billingProfileId, w)).orElse(null)
                ).collect(toSet()))
                .build();
    }
}
