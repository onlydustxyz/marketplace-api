package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "bank_accounts", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
public class BankAccountEntity {

    @Id
    UUID billingProfileId;
    String bic;
    String number;
    @OneToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;
    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;


    public static BankAccountEntity of(BillingProfile.Id billingProfileId, BankAccount bankAccount) {
        return BankAccountEntity.builder()
                .bic(bankAccount.bic())
                .number(bankAccount.accountNumber())
                .billingProfileId(billingProfileId.value())
                .build();
    }

    public BankAccount toDomain() {
        return new BankAccount(bic, number);
    }
}
