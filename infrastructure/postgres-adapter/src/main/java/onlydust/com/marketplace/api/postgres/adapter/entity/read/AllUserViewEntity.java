package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Value
@Table(name = "all_users", schema = "iam")
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class AllUserViewEntity {
    @Id
    @NonNull
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;

    String email;

    public Boolean isRegistered() {
        return userId != null;
    }
}
