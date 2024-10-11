package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationReadRepository extends Repository<ApplicationReadEntity, UUID> {

    @Query("""
            SELECT a
            FROM ApplicationReadEntity a
            JOIN FETCH a.issue i
            JOIN FETCH i.repo
            JOIN FETCH i.author
            JOIN FETCH a.applicant
            WHERE a.id = :id
            """)
    Optional<ApplicationReadEntity> findById(UUID id);

    @Query("""
            SELECT a
            FROM ApplicationReadEntity a
            JOIN FETCH a.issue i
            JOIN FETCH i.repo
            JOIN FETCH i.author
            JOIN FETCH a.project
            JOIN FETCH a.applicant u
            WHERE   (:projectId IS NULL OR a.projectId = :projectId) AND
                    (:issueId IS NULL OR a.issueId = :issueId) AND
                    (:applicantId IS NULL OR a.applicantId = :applicantId) AND
                    (:isApplicantProjectMember IS NULL OR
                        (:isApplicantProjectMember = TRUE AND exists (select 1 from ProjectMemberReadEntity m where m.projectId = a.projectId and m.githubUserId = a.applicantId)) OR 
                        (:isApplicantProjectMember = FALSE AND NOT exists (select 1 from ProjectMemberReadEntity m where m.projectId = a.projectId and m.githubUserId = a.applicantId))
                    ) AND
                    (:applicantLoginSearch IS NULL OR u.login ILIKE %:applicantLoginSearch%)
            """)
    Page<ApplicationReadEntity> findAll(UUID projectId,
                                        Long issueId,
                                        Long applicantId,
                                        Boolean isApplicantProjectMember,
                                        String applicantLoginSearch,
                                        Pageable pageable);

    @Query("""
            SELECT a
            FROM ApplicationReadEntity a
            LEFT JOIN ProjectMemberReadEntity m on m.projectId = a.projectId and m.githubUserId = a.applicantId
            WHERE a.issueId = :issueId AND
                  (:isIgnored IS NULL OR (:isIgnored = TRUE and a.ignoredAt is not null) OR (:isIgnored = FALSE and a.ignoredAt is null)) AND
                  (:isApplicantProjectMember IS NULL OR (:isApplicantProjectMember = TRUE and m.githubUserId is not null) OR (:isApplicantProjectMember = FALSE and m.githubUserId is null)) AND
                  (:search IS NULL OR a.applicant.login ILIKE %:search%)
            """)
    List<ApplicationReadEntity> findAllByIssueId(@NonNull Long issueId,
                                                 Boolean isIgnored,
                                                 Boolean isApplicantProjectMember,
                                                 String search);
}
