package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "user_billing_profile_types", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
public class UserBillingProfileTypeEntity {
    @Id
    UUID userId;
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    BillingProfileTypeEntity billingProfileType;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public enum BillingProfileTypeEntity {
        INDIVIDUAL, COMPANY;
    }


}
