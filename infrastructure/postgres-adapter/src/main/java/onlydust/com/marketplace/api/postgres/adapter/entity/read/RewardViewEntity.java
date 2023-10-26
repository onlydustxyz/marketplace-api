package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
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


}
