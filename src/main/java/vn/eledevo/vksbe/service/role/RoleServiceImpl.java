package vn.eledevo.vksbe.service.role;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.entity.Roles;
import vn.eledevo.vksbe.repository.RoleRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;

    @Override
    public Boolean roleNameChangeDetector(Long roleId, String roleName) {
        Optional<Roles> role = roleRepository.findById(roleId);
        return role.isPresent() && role.get().getName().equals(roleName);
    }
}
