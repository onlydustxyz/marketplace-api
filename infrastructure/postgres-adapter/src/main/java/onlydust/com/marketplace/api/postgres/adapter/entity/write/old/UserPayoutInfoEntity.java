package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(schema = "public", name = "user_payout_info")
public class UserPayoutInfoEntity {

    @Id
    @Column(name = "user_id")
    UUID userId;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @Builder.Default
    Set<OldWalletEntity> wallets = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    OldBankAccountEntity bankAccount;

    public void addWallets(final OldWalletEntity oldWalletEntity) {
        this.wallets.add(oldWalletEntity);
    }
}
