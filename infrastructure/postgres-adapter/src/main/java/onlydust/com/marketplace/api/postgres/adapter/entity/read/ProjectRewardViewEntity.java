package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class ProjectRewardViewEntity {

  @Id
  @Column(name = "id")
  UUID id;
  @Column(name = "requested_at")
  Date requestedAt;
  @Column(name = "login")
  String login;
  @Column(name = "avatar_url")
  String avatarUrl;
  @Column(name = "amount")
  BigDecimal amount;
  @Enumerated(EnumType.STRING)
  @Type(type = "currency")
  @Column(name = "currency")
  CurrencyEnumEntity currency;
  @Column(name = "contribution_count")
  Integer contributionCount;
  @Column(name = "dollars_equivalent")
  BigDecimal dollarsEquivalent;
  @Column(name = "status")
  String status;
}
