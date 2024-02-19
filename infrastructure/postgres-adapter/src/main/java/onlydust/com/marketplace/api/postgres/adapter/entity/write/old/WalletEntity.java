package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "wallets", schema = "public")
@TypeDef(name = "wallet_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@IdClass(WalletEntity.PrimaryKey.class)
public class WalletEntity {
    @Id
    @Column(name = "user_id")
    UUID userId;
    @Id
    @Column(name = "network")
    @Enumerated(EnumType.STRING)
    @Type(type = "network")
    NetworkEnumEntity network;

    @Column(name = "address", nullable = false)
    String address;
    @Column(name = "type", nullable = false)
    @Type(type = "wallet_type")
    @Enumerated(EnumType.STRING)
    WalletTypeEnumEntity type;

    public Invoice.Wallet forInvoice() {
        return new Invoice.Wallet(network.toString(), address);
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        NetworkEnumEntity network;
    }
}
