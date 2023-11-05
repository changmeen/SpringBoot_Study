package rcm.rcmarket;

import rcm.rcmarket.entity.member.Role;
import rcm.rcmarket.entity.member.RoleType;
import rcm.rcmarket.repository.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("local") // 1
public class InitDB {
    private final RoleRepository roleRepository;

    @PostConstruct // 2
    public void initDB() {
        log.info("initialize database");
        initRole(); // 3
    }

    private void initRole() {
        roleRepository.saveAll(
                List.of(RoleType.values()).stream().map(roleType -> new Role(roleType)).collect(Collectors.toList())
        );
    }
}
