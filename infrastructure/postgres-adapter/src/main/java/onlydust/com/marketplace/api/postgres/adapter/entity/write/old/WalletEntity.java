package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "wallets", schema = "public")
@TypeDef(name = "wallet_type", typeClass = PostgreSQLEnumType.class)
public class WalletEntity {

    @EmbeddedId
    WalletIdEntity id;
    @Column(name = "address",nullable = false)
    String address;
    @Column(name = "type",nullable = false)
    @Type(type = "wallet_type")
    @Enumerated(EnumType.STRING)
    WalletTypeEnumEntity type;

}
