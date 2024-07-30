package onlydust.com.marketplace.api.postgres.adapter.entity.enums;

import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;

public enum AllocatedTimeEnumEntity {
    none, less_than_one_day, one_to_three_days, greater_than_three_days;

    public UserAllocatedTimeToContribute toDomain() {
        return switch (this) {
            case none -> UserAllocatedTimeToContribute.NONE;
            case less_than_one_day -> UserAllocatedTimeToContribute.LESS_THAN_ONE_DAY;
            case one_to_three_days -> UserAllocatedTimeToContribute.ONE_TO_THREE_DAYS;
            case greater_than_three_days -> UserAllocatedTimeToContribute.GREATER_THAN_THREE_DAYS;
        };
    }
}
