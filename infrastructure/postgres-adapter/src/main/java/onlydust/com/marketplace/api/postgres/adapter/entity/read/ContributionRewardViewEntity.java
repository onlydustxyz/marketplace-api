package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.view.ContributionRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class ContributionRewardViewEntity {

    @Id
    UUID id;
    Date requestedAt;
    Date processedAt;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    CurrencyEnumEntity currency;
    BigDecimal dollarsEquivalent;
    String status;

    String requestorLogin;
    String requestorAvatarUrl;
    Long requestorId;

    String recipientLogin;
    String recipientAvatarUrl;
    Long recipientId;

    public ContributionRewardView toView() {
        final var requestor = GithubUserIdentity.builder()
                .githubLogin(requestorLogin)
                .githubAvatarUrl(requestorAvatarUrl)
                .githubUserId(requestorId)
                .build();
        final var recipient = GithubUserIdentity.builder()
                .githubLogin(recipientLogin)
                .githubAvatarUrl(recipientAvatarUrl)
                .githubUserId(recipientId)
                .build();

        return ContributionRewardView.builder()
                .id(id)
                .currency(currency.toDomain())
                .amount(amount)
                .dollarsEquivalent(dollarsEquivalent)
                .status(RewardMapper.mapStatusForUser(status))
                .from(requestor)
                .to(recipient)
                .createdAt(requestedAt)
                .processedAt(processedAt)
                .build();
    }
}
