package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "bank_accounts", schema = "public")
public class BankAccountEntity {

    @Id
    @Column(name = "user_id")
    UUID userId;
    @Column(name = "bic")
    String bic;
    @Column(name = "iban")
    String iban;

    public InvoicePreview.BankAccount forInvoicePreview() {
        return new InvoicePreview.BankAccount(bic, iban);
    }
}
