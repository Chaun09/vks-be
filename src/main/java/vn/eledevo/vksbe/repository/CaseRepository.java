package vn.eledevo.vksbe.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.dto.model.account.AccountDownloadResponse;
import vn.eledevo.vksbe.dto.response.account.AccountFilterCaseResponse;
import vn.eledevo.vksbe.dto.response.cases.CasesDashBoard;
import vn.eledevo.vksbe.entity.Cases;

public interface CaseRepository extends BaseRepository<Cases, Long> {
    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT c " + "FROM Cases c " + "WHERE c.id = :id and c.departments.id = :departmentId")
    Optional<Cases> findByCaseAndDepartment(Long id, Long departmentId);

    @Query("select COUNT(ac) > 0 FROM AccountCase ac " + "where ac.accounts.id = :accountId "
            + "and ac.cases.id = :caseId "
            + "and ac.hasAccess = true ")
    boolean checkCaseAccess(Long accountId, Long caseId);

    @Query(
            "SELECT DISTINCT new vn.eledevo.vksbe.dto.model.account.AccountDownloadResponse(a.id, a.username, p.fullName, r.name, a.status) "
                    + "FROM Accounts a "
                    + "JOIN Profiles p ON a.id = p.accounts.id "
                    + "JOIN Roles r ON a.roles.id = r.id "
                    + "JOIN AccountCase acs ON a.id = acs.accounts.id "
                    + "JOIN Cases cs ON cs.id = acs.cases.id "
                    + "WHERE cs.id = :id "
                    + "AND acs.hasAccess = true "
                    + "AND acs.hasPermissionDownload = false "
                    + "AND ((COALESCE(:textSearch, NULL) IS NULL OR LOWER(a.username) LIKE %:textSearch% ) OR LOWER(p.fullName) LIKE %:textSearch%)")
    List<AccountDownloadResponse> findAccountsNoDownload(@Param("id") Long caseId, String textSearch);

    @Query(
            "SELECT DISTINCT new vn.eledevo.vksbe.dto.model.account.AccountDownloadResponse(a.id, a.username, p.fullName, r.name, a.status) "
                    + "FROM Accounts a "
                    + "JOIN Profiles p ON a.id = p.accounts.id "
                    + "JOIN Roles r ON a.roles.id = r.id "
                    + "JOIN AccountCase acs ON a.id = acs.accounts.id "
                    + "JOIN Cases cs ON cs.id = acs.cases.id "
                    + "WHERE cs.id = :id "
                    + "AND acs.hasAccess = true "
                    + "AND acs.hasPermissionDownload = true")
    List<AccountDownloadResponse> findAccountsHasPermissionDownload(@Param("id") Long caseId);

    @Query("SELECT DISTINCT new vn.eledevo.vksbe.dto.response.account.AccountFilterCaseResponse("
            + "a.id, a.username, p.fullName, p.avatar, r.name, p.gender, true, p.uriName) "
            + "FROM AccountCase ac "
            + "JOIN Accounts a ON ac.accounts.id = a.id "
            + "JOIN Profiles p ON p.accounts.id = a.id "
            + "JOIN Roles r ON r.id = a.roles.id "
            + "WHERE ac.cases.id = :caseId AND ac.hasAccess = true AND ac.isInCharge = true")
    List<AccountFilterCaseResponse> getUserInChargeOfflineList(@Param("caseId") Long caseId);

    @Query("SELECT DISTINCT new vn.eledevo.vksbe.dto.response.account.AccountFilterCaseResponse("
            + "a.id, a.username, p.fullName, p.avatar, r.name, p.gender, true, p.uriName) "
            + "FROM AccountCase ac "
            + "JOIN Accounts a ON ac.accounts.id = a.id "
            + "JOIN Profiles p ON p.accounts.id = a.id "
            + "JOIN Roles r ON r.id = a.roles.id "
            + "WHERE ac.cases.id = :caseId AND ac.hasAccess = true AND ac.isProsecutor = true")
    List<AccountFilterCaseResponse> getProsecutorOfflineList(@Param("caseId") Long caseId);

    @Query(value = "SELECT YEAR(created_at) AS year, MONTH(created_at) AS month ,COUNT(*) AS total FROM cases WHERE case_type = :case_type GROUP BY YEAR(created_at), MONTH(created_at) ORDER BY year DESC, month DESC;", nativeQuery = true)
    ArrayList getAllCase(@Param("case_type") String case_type);

}
