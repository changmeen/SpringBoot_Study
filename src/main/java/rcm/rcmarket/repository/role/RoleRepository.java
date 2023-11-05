package rcm.rcmarket.repository.role;

import rcm.rcmarket.entity.member.Role;
import rcm.rcmarket.entity.member.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleType(RoleType roleType);
}
