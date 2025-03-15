package vn.eledevo.vksbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.dto.response.department.DepartmentResponse;
import vn.eledevo.vksbe.entity.Departments;

public interface DepartmentRepository extends BaseRepository<Departments, Long> {

    @Query("SELECT new vn.eledevo.vksbe.dto.response.department.DepartmentResponse("
            + "d.id, d.name, d.code, "
            + "MAX(CASE WHEN a.status = 'ACTIVE' AND r.code IN ('TRUONG_PHONG', 'VIEN_TRUONG') THEN p.fullName END), "
            + "o.name, d.createdAt, d.updatedAt) "
            + "FROM Departments d "
            + "LEFT JOIN Accounts a ON a.departments.id = d.id "
            + "LEFT JOIN Profiles p ON p.accounts.id = a.id "
            + "LEFT JOIN Roles r ON r.id = a.roles.id "
            + "LEFT JOIN Organizations o ON 1=1 "
            + "WHERE o.id = 1 "
            + "GROUP BY d.id, d.name, d.code, o.name, d.createdAt, d.updatedAt")
    List<DepartmentResponse> getDepartmentList();

    boolean existsDepartmentsByNameAndIdNot(String name, Long id);

    Optional<Departments> findDepartmentsByName(String name);

    @Query(value = "SELECT name FROM departments WHERE id = :id", nativeQuery = true)
    String getDepartmentNameById(@Param("id") Long id);
}
