package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.accounting.domain.view.ShortRewardDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode
@Entity
@Value
@Immutable
public class ShortRewardViewEntity {
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
    CurrencyEntity currency;

    public ShortRewardDetailsView toDomain() {
        return ShortRewardDetailsView.builder()
                .id(RewardId.of(this.id))
                .project(ProjectShortView.builder()
                        .id(ProjectId.of(this.projectId))
                        .name(this.projectName)
                        .logoUrl(this.projectLogoUrl)
                        .shortDescription(this.projectShortDescription)
                        .slug(this.projectSlug)
                        .build())
                .recipient(recipient.toDomain())
                .requester(requester.toDomain())
                .money(new MoneyView(this.amount, this.currency.toDomain()))
                .build();
    }

}
