package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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
    @Enumerated(EnumType.STRING)
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
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    BankAccountEntity bankAccount;

    public void addWallets(final WalletEntity walletEntity) {
        this.wallets.add(walletEntity);
    }
}
