package onlydust.com.marketplace.accounting.domain.model.accountbook;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;

import java.util.Collection;

public interface AccountBook {
    void mint(AccountId account, PositiveAmount amount);

    Collection<Transaction> burn(AccountId account, PositiveAmount amount);

    void transfer(AccountId from, AccountId to, PositiveAmount amount);

    void refund(AccountId from, AccountId to, PositiveAmount amount);

    void refund(AccountId from);

    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    class AccountId {
        public static final AccountId ROOT = new AccountId(null, null);

        enum Type {SPONSOR_ACCOUNT, REWARD, PROJECT}

        private Type type;
        private String id;

        public static AccountId of(SponsorAccount.Id id) {
            return new AccountId(Type.SPONSOR_ACCOUNT, id.toString());
        }

        public static AccountId of(ProjectId id) {
            return new AccountId(Type.PROJECT, id.toString());
        }

        public static AccountId of(RewardId id) {
            return new AccountId(Type.REWARD, id.toString());
        }

        public static <T> AccountId of(T id) {
            if (id instanceof SponsorAccount.Id sponsorAccountId) {
                return of(sponsorAccountId);
            } else if (id instanceof ProjectId projectId) {
                return of(projectId);
            } else if (id instanceof RewardId rewardId) {
                return of(rewardId);
            } else {
                throw new IllegalArgumentException("Unsupported id type: " + id.getClass());
            }
        }

        public SponsorAccount.Id sponsorAccountId() {
            if (!isSponsorAccount())
                throw new IllegalArgumentException("Only sponsor accounts can be converted to sponsor account id");

            return SponsorAccount.Id.of(id);
        }

        public ProjectId projectId() {
            if (!isProject())
                throw new IllegalArgumentException("Only projects can be converted to project id");

            return ProjectId.of(id);
        }

        public RewardId rewardId() {
            if (!isReward())
                throw new IllegalArgumentException("Only rewards can be converted to reward id");

            return RewardId.of(id);
        }

        public boolean isReward() {
            return type == Type.REWARD;
        }

        public boolean isProject() {
            return type == Type.PROJECT;
        }

        public boolean isSponsorAccount() {
            return type == Type.SPONSOR_ACCOUNT;
        }
    }

    record Transaction(AccountId from, AccountId to, @NonNull PositiveAmount amount) {
    }
}
