package vn.eledevo.vksbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import vn.eledevo.vksbe.entity.AccountCase;

public interface AccountCaseRepository extends BaseRepository<AccountCase, Long> {

    Optional<AccountCase> findFirstAccountCaseByAccounts_IdAndCases_Id(Long accountsId, Long casesId);

    @Query(
            "SELECT acc FROM AccountCase acc WHERE acc.accounts.id = :accountId AND acc.cases.id = :caseId AND acc.hasAccess = true ")
    Optional<AccountCase> getAccountAccessTrue(Long accountId, Long caseId);

    @Query("SELECT ac FROM AccountCase ac WHERE ac.accounts.id IN :listIdsCreate AND ac.cases.id = :caseId")
    List<AccountCase> findAccountCasesByAccountIdsAndCaseId(
            @Param("listIdsCreate") List<Long> listIdsCreate, @Param("caseId") Long caseId);

    @Query(
            "SELECT COUNT(ac) > 0 FROM AccountCase ac JOIN ac.accounts a WHERE ac.hasAccess = true AND a.isCreateCase = true AND a.id = :userId AND ac.cases.id = :caseId")
    boolean existsByHasAccessAndIsCreateCaseForUser(@Param("userId") Long userId, @Param("caseId") Long caseId);

    Optional<AccountCase> findByCases_IdAndAccounts_Id(Long caseId, Long accountId);

    @Query("SELECT ac FROM AccountCase ac where ac.accounts.id = :accountId AND ac.cases.id = :caseId")
    AccountCase findByAccountId(@Param("accountId") Long accountId, @Param("caseId") Long caseId);

    @Transactional
    @Modifying
    @Query("UPDATE AccountCase ac SET ac.hasPermissionDownload = false "
            + "WHERE ac.accounts.id = :accountId AND ac.accounts.departments.id = :departmentId")
    void updateHasPermissionDownloadFalseByAccountIdAndDepartmentId(
            @Param("accountId") Long accountId, @Param("departmentId") Long departmentId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE account_case ac " + "JOIN cases c ON ac.case_id = c.id "
                    + "SET ac.has_permission_download = false "
                    + "WHERE ac.account_id = :accountId AND c.department_id = :departmentId",
            nativeQuery = true)
    void updateHasPermissionDownloadFalseByAccountId(
            @Param("accountId") Long accountId, @Param("departmentId") Long departmentId);

    @Transactional
    @Modifying
    @Query("UPDATE AccountCase ac SET ac.hasPermissionDownload = true WHERE ac.accounts.id = :accountId")
    void updateHasPermissionDownloadTrueByAccountId(@Param("accountId") Long accountId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE account_case ac " + "JOIN cases c ON ac.case_id = c.id "
                    + "SET ac.has_access = false, ac.is_prosecutor = false, ac.is_in_charge = false, ac.has_permission_download = false "
                    + "WHERE ac.account_id = :accountId AND c.department_id <> :departmentId",
            nativeQuery = true)
    void updateAccountCaseByAccountIdAndDepartmentId(
            @Param("accountId") Long accountId, @Param("departmentId") Long departmentId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE account_case ac " + "JOIN cases c ON ac.case_id = c.id "
                    + "SET ac.has_access = false, ac.is_prosecutor = false, ac.is_in_charge = false, ac.has_permission_download = false "
                    + "WHERE ac.account_id = :accountId AND c.department_id = :departmentId",
            nativeQuery = true)
    void updateAccountCaseByAccountId(@Param("accountId") Long accountId, @Param("departmentId") Long departmentId);
}
