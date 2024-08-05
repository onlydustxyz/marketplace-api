package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.BillingProfilePayoutInfoResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.WalletEntity;
import org.hibernate.annotations.Immutable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(schema = "accounting", name = "payout_infos")
@Immutable
public class PayoutInfoReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;

    @OneToMany
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    @NonNull
    Set<WalletEntity> wallets;

    @OneToOne
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    BankAccountReadEntity bankAccount;

    public BillingProfilePayoutInfoResponse toBoResponse() {
        return new BillingProfilePayoutInfoResponse()
                .bankAccount(bankAccount().map(BankAccountReadEntity::toBillingProfilePayoutInfoResponseBankAccount).orElse(null))
                .ethWallet(ethWallet().map(WalletEntity::getAddress).orElse(null))
                .optimismAddress(optimismWallet().map(WalletEntity::getAddress).orElse(null))
                .aptosAddress(aptosWallet().map(WalletEntity::getAddress).orElse(null))
                .starknetAddress(starknetWallet().map(WalletEntity::getAddress).orElse(null))
                ;
    }

    private Optional<BankAccountReadEntity> bankAccount() {
        return Optional.ofNullable(bankAccount);
    }

    private Optional<WalletEntity> ethWallet() {
        return wallets.stream().filter(wallet -> wallet.getNetwork() == NetworkEnumEntity.ETHEREUM).findFirst();
    }

    private Optional<WalletEntity> optimismWallet() {
        return wallets.stream().filter(wallet -> wallet.getNetwork() == NetworkEnumEntity.OPTIMISM).findFirst();
    }

    private Optional<WalletEntity> aptosWallet() {
        return wallets.stream().filter(wallet -> wallet.getNetwork() == NetworkEnumEntity.APTOS).findFirst();
    }

    private Optional<WalletEntity> starknetWallet() {
        return wallets.stream().filter(wallet -> wallet.getNetwork() == NetworkEnumEntity.STARKNET).findFirst();
    }
}
