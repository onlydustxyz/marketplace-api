package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import java.util.Collection;

public interface AccountBook {
    void mint(AccountId account, PositiveAmount amount);

    Collection<Transaction> burn(AccountId account, PositiveAmount amount);

    void transfer(AccountId from, AccountId to, PositiveAmount amount);

    void refund(AccountId from, AccountId to, PositiveAmount amount);

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    class AccountId {
        public static final AccountId ROOT = new AccountId(null, null);

        enum Type {SPONSOR_ACCOUNT, REWARD, PROJECT}

        private Type type;
        private String id;

        public static AccountId of(Ledger.Id id) {
            return new AccountId(Type.SPONSOR_ACCOUNT, id.toString());
        }

        public static AccountId of(ProjectId id) {
            return new AccountId(Type.PROJECT, id.toString());
        }

        public static AccountId of(RewardId id) {
            return new AccountId(Type.REWARD, id.toString());
        }

        public static <T> AccountId of(T id) {
            if (id instanceof Ledger.Id ledgerId) {
                return of(ledgerId);
            } else if (id instanceof ProjectId projectId) {
                return of(projectId);
            } else if (id instanceof RewardId rewardId) {
                return of(rewardId);
            } else {
                throw new IllegalArgumentException("Unsupported id type: " + id.getClass());
            }
        }

        public Ledger.Id sponsorAccountId() {
            if (type != Type.SPONSOR_ACCOUNT) {
                throw new IllegalArgumentException("Only sponsor accounts can be converted to sponsor account id");
            }
            return Ledger.Id.of(id);
        }

        public ProjectId projectId() {
            if (type != Type.PROJECT) {
                throw new IllegalArgumentException("Only projects can be converted to project id");
            }
            return ProjectId.of(id);
        }

        public RewardId rewardId() {
            if (type != Type.REWARD) {
                throw new IllegalArgumentException("Only rewards can be converted to reward id");
            }
            return RewardId.of(id);
        }
    }

    record Transaction(AccountId from, AccountId to, @NonNull PositiveAmount amount) {
    }
}
