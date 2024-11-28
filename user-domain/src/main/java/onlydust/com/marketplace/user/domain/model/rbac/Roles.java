package onlydust.com.marketplace.user.domain.model.rbac;

public enum Roles {
    BASIC(new Role()
            .on(Resource.CONTRIBUTOR).can(Permission.READ)
            .on(Resource.PROJECT).can(Permission.READ)),

    CONTRIBUTOR(new Role().inherit(BASIC.role)
            .on(Resource.CONTRIBUTOR).can(
                    Permission.READ_SENSITIVE,
                    Permission.WRITE,
                    Permission.READ_FINANCIALS,
                    Permission.WRITE_FINANCIALS)),

    PROJECT_MAINTAINER(new Role().inherit(BASIC.role)
            .on(Resource.PROJECT).can(
                    Permission.READ_SENSITIVE,
                    Permission.WRITE,
                    Permission.READ_FINANCIALS,
                    Permission.WRITE_FINANCIALS)),
    ;

    private final Role role;

    public Role role() {
        return role;
    }

    Roles(Role role) {
        this.role = role;
    }
}
