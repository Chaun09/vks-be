package vn.eledevo.vksbe.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.dto.request.ComputerRequest;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponseFilter;
import vn.eledevo.vksbe.entity.Computers;

public interface ComputerRepository extends BaseRepository<Computers, Long> {
    List<Computers> findByAccounts_Id(Long accountId);

    @Query(
            "SELECT new vn.eledevo.vksbe.dto.response.computer.ComputerResponseFilter("
                    + "c.id, c.name, p.fullName, c.code, c.status, c.brand, c.type, "
                    + "(CASE WHEN c.note IS NULL THEN '' ELSE c.note END), "
                    + "c.createdAt, c.updatedAt, c.createdBy, c.updatedBy) "
                    + "FROM Computers c "
                    + "LEFT JOIN Accounts a ON c.accounts.id = a.id "
                    + "LEFT JOIN Profiles p ON p.accounts.id = a.id "
                    + "WHERE (:#{#computerRequest.name} IS NULL OR COALESCE(c.name, '') LIKE %:#{#computerRequest.name}%) "
                    + "AND (:#{#computerRequest.accountFullName} IS NULL OR COALESCE(p.fullName, '') LIKE %:#{#computerRequest.accountFullName}%) "
                    + "AND (:#{#computerRequest.status} IS NULL OR :#{#computerRequest.status} = '' OR COALESCE(c.status, '') = :#{#computerRequest.status})")
    Page<ComputerResponseFilter> getComputerList(ComputerRequest computerRequest, Pageable pageable);

    @Query("SELECT c FROM Computers c "
            + "WHERE (((COALESCE(:textSearch, NULL) IS NULL ) "
            + "OR LOWER(c.name) LIKE %:textSearch% "
            + "OR LOWER(c.code) LIKE %:textSearch% ) "
            + "AND c.accounts IS NULL )")
    List<Computers> getByTextSearchAndAccountsIsNull(@Param("textSearch") String textSearch);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    List<Computers> findByIdIn(Set<Long> ids);

    Optional<Computers> findComputersByCode(String code);

    Optional<Computers> findByNameAndIdNot(String name, Long id);

    @Query("SELECT c.id FROM Computers c WHERE c.accounts.id = :accountId")
    List<Long> findComputerIdsByAccountId(@Param("accountId") Long accountId);
}
