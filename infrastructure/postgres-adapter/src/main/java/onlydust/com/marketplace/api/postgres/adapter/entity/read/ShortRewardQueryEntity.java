package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode
@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
public class ShortRewardQueryEntity {
    @Id
    @NonNull
    UUID id;
    @ManyToOne
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId")
    @NonNull
    AllUserViewEntity recipient;
    @ManyToOne
    @JoinColumn(name = "requestorId", referencedColumnName = "userId")
    @NonNull
    AllUserViewEntity requester;
    @NonNull
    UUID projectId;
    @NonNull
    String projectName;
    String projectLogoUrl;
    @NonNull
    String projectShortDescription;
    @NonNull
    String projectSlug;
    @NonNull
    BigDecimal amount;
    @ManyToOne
    @NonNull
    CurrencyViewEntity currency;

    public ShortProjectRewardView toProjectDomain() {
        return ShortProjectRewardView.builder()
                .rewardId(RewardId.of(this.id))
                .projectName(this.projectName)
                .money(new Money(this.amount, CurrencyView.builder()
                        .code(this.currency.code())
                        .id(CurrencyView.Id.of(this.currency.id()))
                        .name(this.currency.name())
                        .decimals(this.currency.decimals())
                        .build()))
                .recipient(ContributorLinkView.builder()
                        .githubUserId(this.recipient.githubUserId())
                        .avatarUrl(this.recipient.avatarUrl())
                        .login(this.recipient.login())
                        .build())
                .build();
    }

}
