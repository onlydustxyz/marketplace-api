package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.Committee.ProjectAnswer;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.view.commitee.JuryAssignmentView;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;
    // Users
    private static final UUID anthoId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");
    private static final UUID olivierId = UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9");
    private static final UUID pacoId = UUID.fromString("f20e6812-8de8-432b-9c31-2920434fe7d0");

    // Projects
    private static final UUID apibaraId = UUID.fromString("2073b3b2-60f4-488c-8a0a-ab7121ed850c");
    private static final UUID bretzelId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
    private static final UUID cairoFoundryId = UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4");
    private static final UUID delugeId = UUID.fromString("ade75c25-b39f-4fdf-a03a-e2391c1bc371");
    private static final UUID freshId = UUID.fromString("dcb3548a-977a-480e-8fb4-423d3f890c04");

    // Sponsors
    private static final UUID cocaColax = UUID.fromString("44c6807c-48d1-4987-a0a6-ac63f958bdae");

    private static final Faker faker = new Faker();
    private UUID committeeId;


    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    CommitteeJuryVoteRepository committeeJuryVoteRepository;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_MARKETING_ADMIN));

        committeeId = fakeCommittee().id().value();
    }

    @Test
    void should_get_budget_allocations() {
    }

    private Committee fakeCommittee() {
        var committee = committeeFacadePort.createCommittee("My committee", ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));
        final var juryCriteria = IntStream.range(1, 10).mapToObj(i -> fakeJuryCriteria()).toList();

        committee = committee.toBuilder()
                .sponsorId(cocaColax)
                .projectQuestions(IntStream.range(1, 10).mapToObj(i -> fakeProjectQuestion()).toList())
                .juryIds(List.of(anthoId, olivierId, pacoId))
                .juryCriteria(juryCriteria)
                .build();

        committeeFacadePort.update(committee);

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_APPLICATIONS);
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, apibaraId, anthoId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, bretzelId, anthoId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, cairoFoundryId, anthoId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, delugeId, anthoId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, freshId, anthoId));

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_VOTES);
        final var assignments = committeeFacadePort.getCommitteeById(committee.id()).juryAssignments();
        assignments.forEach(assignemt -> vote(assignemt, juryCriteria, apibaraId, 1));
        assignments.forEach(assignemt -> vote(assignemt, juryCriteria, bretzelId, 2));
        assignments.forEach(assignemt -> vote(assignemt, juryCriteria, cairoFoundryId, 3));
        assignments.forEach(assignemt -> vote(assignemt, juryCriteria, delugeId, 4));
        assignments.forEach(assignemt -> vote(assignemt, juryCriteria, freshId, 5));

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.CLOSED);

        return committee;
    }

    @NonNull
    private void vote(JuryAssignmentView assignment, List<JuryCriteria> juryCriteria, final UUID projectId, int score) {
        final var votes = juryCriteria.stream().map(juryCriterion -> new CommitteeJuryVoteEntity(
                projectId,
                juryCriterion.id().value(),
                committeeId,
                assignment.user().getId(),
                score
        )).toList();

        if (assignment.projectsAssigned().stream().anyMatch(p -> p.id().equals(projectId)))
            committeeJuryVoteRepository.saveAll(votes);
    }

    @NonNull
    private Committee.Application fakeApplication(Committee committee, UUID projectId, UUID userId) {
        return new Committee.Application(projectId, userId, committee.projectQuestions().stream().map(q -> fakeProjectAnswer(q.id())).toList());
    }

    private ProjectQuestion fakeProjectQuestion() {
        return new ProjectQuestion(faker.lorem().paragraph(), faker.bool().bool());
    }

    private JuryCriteria fakeJuryCriteria() {
        return new JuryCriteria(faker.lorem().paragraph());
    }

    private ProjectAnswer fakeProjectAnswer(@NonNull ProjectQuestion.Id projectQuestionId) {
        return new ProjectAnswer(projectQuestionId, faker.lorem().paragraph());
    }
}
