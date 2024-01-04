package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

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
  @Type(type = "jsonb")
  Identity recipientIdentity;
  @Type(type = "jsonb")
  Location recipientLocation;
  @Type(type = "jsonb")
  List<Wallet> recipientWallets;
  String recipientIban;
  String recipientBic;

  public record Identity(@JsonProperty("Company") BoPaymentEntity.Identity.Company company,
                         @JsonProperty("Person") BoPaymentEntity.Identity.Person person) {

    public PaymentView.Identity toDomain() {
      return new PaymentView.Identity(
          Optional.ofNullable(company).map(Company::toDomain).orElse(null),
          Optional.ofNullable(person).map(Person::toDomain).orElse(null)
      );
    }

    public record Company(String name, Person owner,
                          @JsonProperty("identification_number") String identificationNumber) {

      public UserPayoutInformation.Company toDomain() {
        return UserPayoutInformation.Company.builder()
            .name(name)
            .owner(owner == null ? null : owner.toDomain())
            .identificationNumber(identificationNumber)
            .build();
      }
    }

    public record Person(String firstname, String lastname) {

      public UserPayoutInformation.Person toDomain() {
        return UserPayoutInformation.Person.builder().firstName(firstname).lastName(lastname).build();
      }
    }
  }

  public record Location(String country, String city, String address, @JsonProperty("post_code") String postCode) {

    public UserPayoutInformation.Location toDomain() {
      return UserPayoutInformation.Location.builder()
          .country(country)
          .city(city)
          .address(address)
          .postalCode(postCode)
          .build();
    }
  }

  public record Wallet(String network, String type, String address) {

  }

  public PaymentView toView() {
    final var wallets = Optional.ofNullable(recipientWallets).orElse(List.of());

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
        .recipientIdentity(nonNull(recipientIdentity) ? recipientIdentity.toDomain() : null)
        .recipientLocation(nonNull(recipientLocation) ? recipientLocation.toDomain() : null)
        .recipientSepaAccount(UserPayoutInformation.SepaAccount.builder().accountNumber(AccountNumber.of(recipientIban)).bic(recipientBic).build())
        .recipientEthWallet(
            wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.ethereum.name())).findFirst().map(Wallet::address)
                .orElse(null))
        .recipientStarkWallet(
            wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.starknet.name())).findFirst().map(Wallet::address)
                .orElse(null))
        .recipientOptimismWallet(
            wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.optimism.name())).findFirst().map(Wallet::address)
                .orElse(null))
        .recipientAptosWallet(
            wallets.stream().filter(wallet -> wallet.network().equals(NetworkEnumEntity.aptos.name())).findFirst().map(Wallet::address).orElse(null))
        .build();
  }
}
