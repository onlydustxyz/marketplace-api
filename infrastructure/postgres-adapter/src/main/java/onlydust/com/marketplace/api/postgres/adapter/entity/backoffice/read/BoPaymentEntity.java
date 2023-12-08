package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class BoPaymentEntity {
    @Id
    UUID id;
    UUID budgetId;
    UUID projectId;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    CurrencyEnumEntity currency;
    Long recipientId;
    UUID requestorId;
    @Type(type = "jsonb")
    List<String> items;
    ZonedDateTime requestedAt;
    ZonedDateTime processedAt;
    Integer pullRequestsCount;
    Integer issuesCount;
    Integer dustyIssuesCount;
    Integer codeReviewsCount;

    public PaymentView toView() {
        return PaymentView.builder()
                .id(id)
                .budgetId(budgetId)
                .projectId(projectId)
                .amount(amount)
                .currency(currency.toDomain())
                .recipientId(recipientId)
                .requestorId(requestorId)
                .items(items)
                .requestedAt(requestedAt)
                .processedAt(processedAt)
                .pullRequestsCount(pullRequestsCount)
                .issuesCount(issuesCount)
                .dustyIssuesCount(dustyIssuesCount)
                .codeReviewsCount(codeReviewsCount)
                .build();
    }
}
