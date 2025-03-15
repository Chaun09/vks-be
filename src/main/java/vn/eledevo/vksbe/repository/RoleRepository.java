package vn.eledevo.vksbe.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.entity.Roles;

public interface RoleRepository extends BaseRepository<Roles, Long> {
    boolean existsById(@NotNull Long id);

    @Query(value = "SELECT name FROM roles WHERE id = :id", nativeQuery = true)
    String getRole(@Param("id") Long id);
}
