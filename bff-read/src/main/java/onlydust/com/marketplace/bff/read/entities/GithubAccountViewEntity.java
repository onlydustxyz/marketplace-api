package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ContactInformation;
import onlydust.com.marketplace.api.contract.model.ContactInformationChannel;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Entity
@Table(name = "github_accounts", schema = "indexer_exp")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class GithubAccountViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull Long id;

    String login;
    @Enumerated(EnumType.STRING)
    Type type;
    String htmlUrl;
    String name;
    String bio;
    String location;
    String website;
    String twitter;
    String linkedin;
    String telegram;
    @NonNull ZonedDateTime createdAt;

    public enum Type {
        USER, ORGANIZATION, BOT
    }

    public List<ContactInformation> contacts() {
        return Stream.of(
                        twitter == null ? null :
                                new ContactInformation().channel(ContactInformationChannel.TWITTER).contact(twitter).visibility(ContactInformation.VisibilityEnum.PUBLIC),
                        linkedin == null ? null :
                                new ContactInformation().channel(ContactInformationChannel.LINKEDIN).contact(linkedin).visibility(ContactInformation.VisibilityEnum.PUBLIC),
                        telegram == null ? null :
                                new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact(telegram).visibility(ContactInformation.VisibilityEnum.PUBLIC)
                ).filter(Objects::nonNull)
                .toList();
    }
}
