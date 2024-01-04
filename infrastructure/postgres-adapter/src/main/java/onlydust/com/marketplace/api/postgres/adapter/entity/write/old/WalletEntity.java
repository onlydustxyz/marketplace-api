package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "wallets", schema = "public")
@TypeDef(name = "wallet_type", typeClass = PostgreSQLEnumType.class)
public class WalletEntity {

  @EmbeddedId
  WalletIdEntity id;
  @Column(name = "address", nullable = false)
  String address;
  @Column(name = "type", nullable = false)
  @Type(type = "wallet_type")
  @Enumerated(EnumType.STRING)
  WalletTypeEnumEntity type;
}
