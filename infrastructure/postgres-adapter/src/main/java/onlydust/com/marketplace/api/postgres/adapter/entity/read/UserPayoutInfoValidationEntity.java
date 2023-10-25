package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class UserPayoutInfoValidationEntity {

    @Id
    @Column(name = "user_id")
    UUID userId;
    @Column(name = "valid_person")
    Boolean hasValidPerson;
    @Column(name = "valid_location")
    Boolean hasValidLocation;
    @Column(name = "valid_company")
    Boolean hasValidCompany;
    @Column(name = "valid_op_wallet")
    Boolean hasValidOptimismWallet;
    @Column(name = "valid_stark_wallet")
    Boolean hasValidStarknetWallet;
    @Column(name = "valid_apt_wallet")
    Boolean hasValidAptosWallet;
    @Column(name = "valid_eth_wallet")
    Boolean hasValidEthWallet;
    @Column(name = "valid_banking_account")
    Boolean hasValidBakingAccount;

}
