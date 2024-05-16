package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "bank_accounts", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class BankAccountViewEntity {

    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;
    String bic;
    String number;
    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    private Date updatedAt;

    public BankAccount toDomain() {
        return new BankAccount(bic, number);
    }
}
