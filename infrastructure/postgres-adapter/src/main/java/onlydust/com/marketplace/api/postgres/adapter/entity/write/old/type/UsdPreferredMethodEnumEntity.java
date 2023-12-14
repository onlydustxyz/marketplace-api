package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;

public enum UsdPreferredMethodEnumEntity {
    fiat,crypto;

    public UserPayoutInformation.UsdPreferredMethodEnum toDomain() {
        return switch (this) {
            case fiat -> UserPayoutInformation.UsdPreferredMethodEnum.FIAT;
            case crypto -> UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO;
        };
    }
}
