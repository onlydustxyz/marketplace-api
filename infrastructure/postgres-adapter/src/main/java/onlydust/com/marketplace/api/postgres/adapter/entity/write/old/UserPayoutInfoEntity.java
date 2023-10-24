package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.UsdPreferredMethodEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

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
@TypeDef(name = "preferred_type", typeClass = PostgreSQLEnumType.class)
public class UserPayoutInfoEntity {

    @Id
    @Column(name = "user_id")
    UUID userId;
    @Type(type = "preferred_type")
    @Enumerated(EnumType.STRING)
    @Column(name = "usd_preferred_method")
    UsdPreferredMethodEnumEntity usdPreferredMethodEnum;
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(columnDefinition = "jsonb", name = "location")
    private JsonNode location;
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(columnDefinition = "jsonb", name = "identity")
    private JsonNode identity;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @Builder.Default
    Set<WalletEntity> wallets = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    BankAccountEntity bankAccount;

    public void addWallets(final WalletEntity walletEntity) {
        this.wallets.add(walletEntity);
    }

    public void removeWallets(final WalletEntity walletEntity) {
        if (this.wallets.contains(walletEntity)) {
            this.wallets.remove(walletEntity);
        }
    }
}
