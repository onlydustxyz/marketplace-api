package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileAdminView;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
public class OldBillingProfileAdminViewEntity {
    @Id
    UUID id;
    String githubLogin;
    String firstName;
    String email;

    public BillingProfileAdminView toDomain(){
        return BillingProfileAdminView.builder()
                .githubLogin(this.githubLogin)
                .firstName(this.firstName)
                .email(this.email)
                .build();
    }
}
