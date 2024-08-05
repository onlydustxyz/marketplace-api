package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.BillingProfilePayoutInfoResponseBankAccount;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "bank_accounts", schema = "accounting")
@Immutable
public class BankAccountReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;
    String bic;
    String number;

    public BillingProfilePayoutInfoResponseBankAccount toBillingProfilePayoutInfoResponseBankAccount() {
        return new BillingProfilePayoutInfoResponseBankAccount()
                .bic(bic)
                .number(number);
    }
}
