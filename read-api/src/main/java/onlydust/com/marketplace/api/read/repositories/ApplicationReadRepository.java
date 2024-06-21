package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationReadRepository extends Repository<ApplicationReadEntity, UUID> {

    @Query("""
            SELECT a
            FROM ApplicationReadEntity a
            JOIN FETCH a.issue
            JOIN FETCH a.applicant
            JOIN FETCH a.ranking
            WHERE a.id = :id
            """)
    Optional<ApplicationReadEntity> findById(UUID id);

    @Query("""
            SELECT a
            FROM ApplicationReadEntity a
            JOIN FETCH a.issue i
            JOIN FETCH a.applicant u
            JOIN FETCH a.ranking
            WHERE   (:projectId IS NULL OR a.projectId = :projectId) AND
                    (:issueId IS NULL OR a.issue.id = :issueId) AND
                    (:applicantId IS NULL OR a.applicant.id = :applicantId) AND
                    (:isApplicantProjectMember IS NULL OR a.applicant.id = :isApplicantProjectMember) AND
                    (:applicantLoginSearch IS NULL OR u.login ILIKE %:applicantLoginSearch%)
            """)
    Page<ApplicationReadEntity> findAll(UUID projectId,
                                        Long issueId,
                                        Long applicantId,
                                        Boolean isApplicantProjectMember,
                                        String applicantLoginSearch,
                                        Pageable pageable);
}
