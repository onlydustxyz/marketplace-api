package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

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
@IdClass(BillingProfileUserQueryEntity.PrimaryKey.class)
@Immutable
public class BillingProfileUserQueryEntity {

    @Id
    UUID billingProfileId;
    @Id
    Long githubUserId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "billing_profile_role")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    BillingProfile.User.Role role;

    UUID userId;
    String email;
    String githubLogin;
    String githubAvatarUrl;
    String githubHtmlUrl;
    String firstName;
    String lastName;
    String country;
    Date joinedAt;
    Date invitedAt;
    Integer rewardCount;
    Integer billingProfileAdminCount;

    public BillingProfileCoworkerView toView() {
        return BillingProfileCoworkerView.builder()
                .userId(userId != null ? UserId.of(userId) : null)
                .role(role)
                .login(githubLogin)
                .email(email)
                .githubUserId(githubUserId != null ? GithubUserId.of(githubUserId) : null)
                .avatarUrl(githubAvatarUrl)
                .githubHtmlUrl(githubHtmlUrl != null ? URI.create(githubHtmlUrl) : null)
                .firstName(firstName)
                .lastName(lastName)
                .country(country)
                .joinedAt(joinedAt != null ? ZonedDateTime.ofInstant(joinedAt.toInstant(), ZoneOffset.UTC) : null)
                .invitedAt(invitedAt != null ? ZonedDateTime.ofInstant(invitedAt.toInstant(), ZoneOffset.UTC) : null)
                .rewardCount(rewardCount)
                .billingProfileAdminCount(billingProfileAdminCount)
                .build();
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        Long githubUserId;
    }
}
