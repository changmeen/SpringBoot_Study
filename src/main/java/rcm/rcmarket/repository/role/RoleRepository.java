package rcm.rcmarket.repository.role;

import org.springframework.data.jpa.repository.JpaRepository;
import rcm.rcmarket.entity.member.Role;
import rcm.rcmarket.entity.member.RoleType;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleType(RoleType roleType);

}
