package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.databind.JsonNode;
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
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class RewardViewEntity {

  @Id
  @Column(name = "id")
  UUID id;
  @Column(name = "requested_at")
  Date requestedAt;
  @Column(name = "processed_at")
  Date processedAt;
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

  @Column(name = "requestor_login")
  String requestorLogin;
  @Column(name = "requestor_avatar_url")
  String requestorAvatarUrl;
  @Column(name = "requestor_id")
  Long requestorId;

  @Column(name = "recipient_login")
  String recipientLogin;
  @Column(name = "recipient_avatar_url")
  String recipientAvatarUrl;
  @Column(name = "recipient_id")
  Long recipientId;
  @Column(name = "receipt", columnDefinition = "jsonb")
  @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
  JsonNode receipt;

  UUID projectId;
  String projectKey;
  String projectName;
  String projectShortDescription;
  String projectLongDescription;
  String projectLogoUrl;
  String projectTelegramLink;
  Boolean projectHiring;
  @Enumerated(EnumType.STRING)
  @Type(type = "project_visibility")
  ProjectVisibilityEnumEntity projectVisibility;
}
