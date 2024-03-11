package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.project.domain.model.OldAccountNumber;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.project.domain.view.backoffice.PaymentView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BoPaymentEntity {
    @Id
    UUID id;
    UUID budgetId;
    UUID projectId;
    BigDecimal amount;
    @ManyToOne
    CurrencyEntity currency;
    Long recipientId;
    UUID requestorId;
    @Type(type = "jsonb")
    List<String> items;
    ZonedDateTime requestedAt;
    Integer pullRequestsCount;
    Integer issuesCount;
    Integer dustyIssuesCount;
    Integer codeReviewsCount;
    //    @Type(type = "jsonb")
//    Identity recipientIdentity;
//    @Type(type = "jsonb")
//    Location recipientLocation;
    @Type(type = "jsonb")
    List<Wallet> recipientWallets;
    String recipientIban;
    String recipientBic;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusDataEntity statusData;


    // TODO : use KYC/KYB data
//    public record Identity(@JsonProperty("Company") BoPaymentEntity.Identity.Company company,
//                           @JsonProperty("Person") BoPaymentEntity.Identity.Person person) {
//        public PaymentView.Identity toDomain() {
//            return new PaymentView.Identity(
//                    Optional.ofNullable(company).map(Company::toDomain).orElse(null),
//                    Optional.ofNullable(person).map(Person::toDomain).orElse(null)
//            );
//        }
//
//        public record Company(String name, Person owner,
//                              @JsonProperty("identification_number") String identificationNumber) {
//            public UserPayoutSettings.Company toDomain() {
//                return UserPayoutSettings.Company.builder()
//                        .name(name)
//                        .owner(owner == null ? null : owner.toDomain())
//                        .identificationNumber(identificationNumber)
//                        .build();
//            }
//        }
//
//        public record Person(String firstname, String lastname) {
//            public UserPayoutSettings.Person toDomain() {
//                return UserPayoutSettings.Person.builder().firstName(firstname).lastName(lastname).build();
//            }
//        }
//    }

//    public record Location(String country, String city, String address, @JsonProperty("post_code") String postCode) {
//        public UserPayoutSettings.Location toDomain() {
//            return UserPayoutSettings.Location.builder()
//                    .country(country)
//                    .city(city)
//                    .address(address)
//                    .postalCode(postCode)
//                    .build();
//        }
//    }

    public record Wallet(String network, String type, String address) {
    }

    public PaymentView toView() {
        final var wallets = Optional.ofNullable(recipientWallets).orElse(List.of());

        return PaymentView.builder()
                .id(id)
                .budgetId(budgetId)
                .projectId(projectId)
                .amount(amount)
                .currency(currency.toOldDomain())
                .recipientId(recipientId)
                .requestorId(requestorId)
                .items(items)
                .requestedAt(requestedAt)
                .processedAt(statusData.paidAt().toInstant().atZone(ZoneOffset.UTC))
                .pullRequestsCount(pullRequestsCount)
                .issuesCount(issuesCount)
                .dustyIssuesCount(dustyIssuesCount)
                .codeReviewsCount(codeReviewsCount)
//                .recipientIdentity(nonNull(recipientIdentity) ? recipientIdentity.toDomain() : null)
//                .recipientLocation(nonNull(recipientLocation) ? recipientLocation.toDomain() : null)
                .recipientSepaAccount(UserPayoutSettings.SepaAccount.builder().accountNumber(OldAccountNumber.of(recipientIban)).bic(recipientBic).build())
                .recipientEthWallet(wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.ethereum.name())).findFirst().map(Wallet::address).orElse(null))
                .recipientStarkWallet(wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.starknet.name())).findFirst().map(Wallet::address).orElse(null))
                .recipientOptimismWallet(wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.optimism.name())).findFirst().map(Wallet::address).orElse(null))
                .recipientAptosWallet(wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.aptos.name())).findFirst().map(Wallet::address).orElse(null))
                .build();
    }
}
