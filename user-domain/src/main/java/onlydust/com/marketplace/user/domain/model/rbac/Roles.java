package onlydust.com.marketplace.user.domain.model.rbac;

public enum Roles {
    CONTRIBUTOR_READER(new Role()
            .on(Resource.CONTRIBUTOR).can(Permission.READ)),

    CONTRIBUTOR_ADMIN(new Role().inherit(CONTRIBUTOR_READER.role)
            .on(Resource.CONTRIBUTOR).can(
                    Permission.READ_PRIVATE,
                    Permission.WRITE)),

    PROJECT_READER(new Role()
            .on(Resource.PROJECT).can(Permission.READ)),

    PROJECT_MAINTAINER(new Role().inherit(PROJECT_READER.role)
            .on(Resource.PROJECT).can(
                    Permission.READ_PRIVATE,
                    Permission.WRITE)),

    PROGRAM_READER(new Role()
            .on(Resource.PROGRAM).can(Permission.READ)),

    PROGRAM_ADMIN(new Role().inherit(PROGRAM_READER.role)
            .on(Resource.PROGRAM).can(
                    Permission.READ_PRIVATE,
                    Permission.WRITE));

    private final Role role;

    public Role role() {
        return role;
    }

    Roles(Role role) {
        this.role = role;
    }
}
