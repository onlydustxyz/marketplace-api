package onlydust.com.marketplace.kernel.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class RewardStatusTest {

    @Nested
    class GivenAUser {
        @ParameterizedTest
        @EnumSource(RewardStatus.AsUser.class)
        void should_see_the_status_of_his_reward(RewardStatus.AsUser status) {
            assertThat(new RewardStatus(status).asUser()).isEqualTo(status);
        }
    }

    @Nested
    class GivenAProjectLead {
        @ParameterizedTest
        @EnumSource(value = RewardStatus.AsUser.class, mode = EnumSource.Mode.INCLUDE, names = "PENDING_SIGNUP")
        void should_see_pending_signup(RewardStatus.AsUser status) {
            assertThat(new RewardStatus(status).asProjectLead()).isEqualTo(RewardStatus.AsProjectLead.PENDING_SIGNUP);
        }

        @ParameterizedTest
        @EnumSource(value = RewardStatus.AsUser.class, mode = EnumSource.Mode.INCLUDE, names = "PROCESSING")
        void should_see_pending_processing(RewardStatus.AsUser status) {
            assertThat(new RewardStatus(status).asProjectLead()).isEqualTo(RewardStatus.AsProjectLead.PROCESSING);
        }

        @ParameterizedTest
        @EnumSource(value = RewardStatus.AsUser.class, mode = EnumSource.Mode.INCLUDE, names = "COMPLETE")
        void should_see_pending_complete(RewardStatus.AsUser status) {
            assertThat(new RewardStatus(status).asProjectLead()).isEqualTo(RewardStatus.AsProjectLead.COMPLETE);
        }

        @ParameterizedTest
        @EnumSource(value = RewardStatus.AsUser.class, mode = EnumSource.Mode.EXCLUDE, names = {"PENDING_SIGNUP", "PROCESSING", "COMPLETE"})
        void should_see_pending_pending_contributor(RewardStatus.AsUser status) {
            assertThat(new RewardStatus(status).asProjectLead()).isEqualTo(RewardStatus.AsProjectLead.PENDING_CONTRIBUTOR);
        }
    }
}