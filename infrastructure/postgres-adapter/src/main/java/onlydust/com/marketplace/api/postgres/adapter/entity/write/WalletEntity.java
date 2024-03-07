package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "wallets", schema = "accounting")
@TypeDef(name = "wallet_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@IdClass(WalletEntity.PrimaryKey.class)
@EntityListeners(AuditingEntityListener.class)
public class WalletEntity {
    @Id
    UUID billingProfileId;
    @Id
    @Column(name = "network")
    @Enumerated(EnumType.STRING)
    @Type(type = "network")
    NetworkEnumEntity network;

    @ManyToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;

    @Column(name = "address", nullable = false)
    String address;
    @Column(name = "type", nullable = false)
    @Type(type = "wallet_type")
    @Enumerated(EnumType.STRING)
    WalletTypeEnumEntity type;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;


    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        NetworkEnumEntity network;
    }
}
