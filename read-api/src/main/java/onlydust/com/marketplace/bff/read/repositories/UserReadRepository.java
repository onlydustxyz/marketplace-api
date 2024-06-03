package onlydust.com.marketplace.bff.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserReadRepository extends JpaRepository<AllUserReadEntity, UUID> {
    @Query(value = """
            SELECT u
            FROM AllUserReadEntity u
            JOIN FETCH u.registered
            WHERE (:login is null or u.login ILIKE CONCAT('%',:login,'%'))
            """)
    @NotNull
    Page<AllUserReadEntity> findAllRegisteredByLogin(final String login, final @NotNull Pageable pageable);


    @Query(value = """
            SELECT u
            FROM AllUserReadEntity u
            JOIN FETCH u.registered
            JOIN u.hackathonRegistrations hr
            WHERE hr.hackathonId = :hackathonId AND
                 u.login ILIKE CONCAT('%',:login,'%')
            """)
    @NotNull
    Page<AllUserReadEntity> findAllRegisteredOnHackathon(final String login, @NonNull UUID hackathonId, final @NotNull Pageable pageable);
}
