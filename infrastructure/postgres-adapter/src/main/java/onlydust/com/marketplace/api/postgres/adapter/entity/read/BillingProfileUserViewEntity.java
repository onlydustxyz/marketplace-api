package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@IdClass(BillingProfileUserViewEntity.PrimaryKey.class)
@TypeDef(name = "billing_profile_role", typeClass = PostgreSQLEnumType.class)
public class BillingProfileUserViewEntity {

    @Id
    UUID billingProfileId;
    @Id
    UUID userId;

    @Column(name = "role", nullable = false)
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    BillingProfileUserEntity.Role role;

    Long githubUserId;
    String githubLogin;
    String githubAvatarUrl;
    String githubHtmlUrl;
    Date joinedAt;
    Date invitedAt;
    Integer rewardCount;

    public BillingProfileCoworkerView toView() {
        return BillingProfileCoworkerView.builder()
                .userId(UserId.of(userId))
                .role(role.toDomain())
                .login(githubLogin)
                .githubUserId(githubUserId)
                .avatarUrl(githubAvatarUrl)
                .githubHtmlUrl(githubHtmlUrl != null ? URI.create(githubHtmlUrl) : null)
                .joinedAt(joinedAt != null ? ZonedDateTime.ofInstant(joinedAt.toInstant(), ZoneOffset.UTC) : null)
                .invitedAt(invitedAt != null ? ZonedDateTime.ofInstant(invitedAt.toInstant(), ZoneOffset.UTC) : null)
                .rewardCount(rewardCount)
                .build();
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID billingProfileId;
    }
}
