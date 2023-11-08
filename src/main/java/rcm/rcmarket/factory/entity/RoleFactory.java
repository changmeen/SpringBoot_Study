package rcm.rcmarket.factory.entity;

import rcm.rcmarket.entity.member.Role;
import rcm.rcmarket.entity.member.RoleType;

public class RoleFactory {
    public static Role createRole() {
        return new Role(RoleType.ROLE_NORMAL);
    }
}
