package vn.eledevo.vksbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import vn.eledevo.vksbe.dto.model.account.AccountQueryToFilter;
import vn.eledevo.vksbe.dto.model.account.UserInfo;
import vn.eledevo.vksbe.dto.request.AccountActive;
import vn.eledevo.vksbe.dto.request.AccountRequest;
import vn.eledevo.vksbe.dto.request.account.AccountCaseRequest;
import vn.eledevo.vksbe.dto.response.account.AccountCaseResponse;
import vn.eledevo.vksbe.dto.response.account.AccountSwapResponse;
import vn.eledevo.vksbe.dto.response.account.StakeHolderResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Profiles;
import vn.eledevo.vksbe.entity.Roles;

public interface AccountRepository extends BaseRepository<Accounts, Long> {
    @Query("SELECT a,r.code from Accounts a inner join Roles r on a.roles.id = r.id  where a.username =:username")
    Optional<Accounts> findAccountInSystem(String username);

    @Transactional
    @Query("SELECT new vn.eledevo.vksbe.dto.model.account.AccountQueryToFilter("
            + "a.id, a.username, p.fullName, r.name, r.code, r.id, d.id, d.name, o.id, "
            + "o.name, a.status, a.isConnectComputer, "
            + "a.isConnectUsb, a.createdAt, a.updatedAt, a.isCreateCase, false , false, false , false, false , false, false , false) "
            + "FROM Accounts a "
            + "LEFT JOIN Profiles p ON p.accounts.id = a.id "
            + "LEFT JOIN Roles r ON r.id = a.roles.id "
            + "AND (:isBoss = true OR (r.code = 'TRUONG_PHONG' OR r.code = 'PHO_PHONG' OR r.code = 'KIEM_SAT_VIEN')) "
            + "LEFT JOIN Departments d ON d.id = a.departments.id "
            + "LEFT JOIN Organizations o ON 1=1 "
            + "WHERE (:#{#filter.username} IS NULL OR :#{#filter.username} = '' OR a.username LIKE %:#{#filter.username}%) "
            + "AND (:#{#filter.fullName} IS NULL OR :#{#filter.fullName} = '' OR p.fullName LIKE %:#{#filter.fullName}%) "
            + "AND (:#{#filter.roleId} = 0 OR a.roles.id = :#{#filter.roleId}) "
            + "AND (:#{#filter.departmentId} = 0 OR a.departments.id = :#{#filter.departmentId}) "
            + "AND (:#{#filter.organizationId} = 0 OR o.id = :#{#filter.organizationId}) "
            + "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR a.status = :#{#filter.status}) "
            + "AND ((:#{#filter.fromDate} IS NULL OR :#{#filter.toDate} IS NULL) "
            + "OR DATE(a.updatedAt) BETWEEN :#{#filter.fromDate} AND :#{#filter.toDate}) "
            + "AND o.id = 1 ")
    Page<AccountQueryToFilter> getAccountList(AccountRequest filter, Boolean isBoss, Pageable pageable);

    Optional<Accounts> findByUsername(String username);

    @Query("SELECT new vn.eledevo.vksbe.dto.request.AccountActive(a.id, a.roles.code,"
            + "a.status,a.departments.id) from Accounts a where a.username =:username")
    Optional<AccountActive> findByUsernameActive(String username);

    @Query(
            "SELECT a from Accounts  a where a.roles.code=:roleCode and a.departments.id=:departmentId and a.status=:accountStatus")
    Optional<Accounts> findByDepartment(Long departmentId, String roleCode, String accountStatus);

    boolean existsByUsername(String username);

    @Transactional
    @Query(
            "SELECT distinct new vn.eledevo.vksbe.dto.model.account.UserInfo(a.id,a.username, p.avatar, p.fullName, p.gender, p.phoneNumber, r.code, d.code, o.id,o.name, a.isCreateCase, a.isConditionLogin1, a.isConditionLogin2) "
                    + "FROM Accounts a, Organizations o "
                    + "JOIN a.roles r "
                    + "JOIN a.departments d "
                    + "JOIN a.profile p "
                    + "WHERE a.id =:accountId AND o.id=1")
    Optional<UserInfo> findAccountProfileById(@Param("accountId") Long accountId);

    @Query("SELECT new vn.eledevo.vksbe.dto.response.account.AccountSwapResponse(a.id, a.username, p.fullName) "
            + "FROM Accounts a "
            + "JOIN a.profile p "
            + "WHERE a.departments.id =:departmentId "
            + "AND a.status = 'ACTIVE' "
            + "AND a.roles.id IN(1, 4)")
    AccountSwapResponse getOldPositionAccInfo(@Param("departmentId") Long departmentId);

    @Query("SELECT new vn.eledevo.vksbe.dto.response.account.AccountSwapResponse(a.id, a.username, p.fullName) "
            + "FROM Accounts a "
            + "JOIN a.profile p "
            + "JOIN a.departments d "
            + "WHERE a.status = 'ACTIVE' "
            + "AND a.roles.code =:roleCode "
            + "AND a.departments.code =:departmentCode")
    Optional<AccountSwapResponse> getOldLeader(String roleCode, String departmentCode);

    @Query(
            "SELECT a FROM Accounts a WHERE a.id IN :ids AND (a.departments.id = :iddepart or a.departments.code = 'PB_LANH_DAO') ")
    List<Accounts> findByIdsAndIddepart(@Param("ids") List<Long> ids, @Param("iddepart") Long iddepart);

    @Transactional
    @Query("SELECT new vn.eledevo.vksbe.dto.response.account.AccountCaseResponse "
            + "(c.id, c.code, c.name, d.name, cs.name, ac.hasAccess, c.updatedAt) "
            + "FROM Cases c "
            + "LEFT JOIN AccountCase ac ON ac.cases.id = c.id "
            + "LEFT JOIN AccountCase ac2 ON ac2.cases.id = c.id "
            + "LEFT JOIN AccountCase ac3 ON ac3.cases.id = c.id "
            + "LEFT JOIN CasePerson cp ON cp.cases.id = c.id "
            + "LEFT JOIN Departments d ON c.departments.id = d.id "
            + "LEFT JOIN CaseStatus cs ON cs.id = c.case_status.id "
            + "WHERE ac.accounts.id = :accountId "
            + "AND ((COALESCE(:hasAccess, NULL) IS NULL) OR ac.hasAccess = :hasAccess) "
            + "AND ((COALESCE(:#{#req.departmentId}, 0) = 0) OR d.id = :#{#req.departmentId}) "
            + "AND (COALESCE(:#{#req.textSearch}, '') = '' OR LOWER(c.name) LIKE %:#{#req.textSearch}% OR LOWER(c.code) LIKE %:#{#req.textSearch}%) "
            + "AND ((COALESCE(:#{#req.userInChargeId}, 0) = 0) OR (ac2.accounts.id = :#{#req.userInChargeId} AND ac2.isInCharge = true)) "
            + "AND ((COALESCE(:#{#req.prosecutorId}, 0) = 0) OR (ac3.accounts.id = :#{#req.prosecutorId} AND ac3.isProsecutor = true)) "
            + "AND ((COALESCE(:#{#req.citizenId}, 0) = 0) OR (cp.citizens.id = :#{#req.citizenId})) "
            + "AND ((COALESCE(:#{#req.statusId}, 0) = 0) OR cs.id = :#{#req.statusId}) "
            + "AND ((COALESCE(:#{#req.getTimeFromDate()}, NULL) IS NULL) OR c.updatedAt >= :#{#req.getTimeFromDate()}) "
            + "AND ((COALESCE(:#{#req.getTimeToDate()}, NULL) IS NULL) OR c.updatedAt < :#{#req.getTimeToDate()}) "
            + "group by c.id, c.code, c.name, d.name, cs.name, c.updatedAt, ac.hasAccess")
    Page<AccountCaseResponse> getAccountCaseByHeadRole(
            @Param("accountId") Long accountId,
            @Param("hasAccess") Boolean hasAccess,
            AccountCaseRequest req,
            Pageable pageable);

    @Transactional
    @Query("SELECT new vn.eledevo.vksbe.dto.response.account.AccountCaseResponse "
            + "(c.id, c.code, c.name, d.name, cs.name, ac.hasAccess, c.updatedAt) "
            + "FROM Cases c "
            + "LEFT JOIN AccountCase ac ON ac.cases.id = c.id "
            + "LEFT JOIN AccountCase ac2 ON ac2.cases.id = c.id "
            + "LEFT JOIN AccountCase ac3 ON ac3.cases.id = c.id "
            + "LEFT JOIN CasePerson cp ON cp.cases.id = c.id "
            + "LEFT JOIN Departments d ON c.departments.id = d.id "
            + "LEFT JOIN CaseStatus cs ON cs.id = c.case_status.id "
            + "WHERE ac.accounts.id = :accountId "
            + "AND d.id = :departmentId "
            + "AND ((COALESCE(:hasAccess, NULL) IS NULL) OR ac.hasAccess = :hasAccess) "
            + "AND (COALESCE(:#{#req.textSearch}, '') = '' OR LOWER(c.name) LIKE %:#{#req.textSearch}% OR LOWER(c.code) LIKE %:#{#req.textSearch}%) "
            + "AND ((COALESCE(:#{#req.userInChargeId}, 0) = 0) OR (ac2.accounts.id = :#{#req.userInChargeId} AND ac2.isInCharge = true)) "
            + "AND ((COALESCE(:#{#req.prosecutorId}, 0) = 0) OR (ac3.accounts.id = :#{#req.prosecutorId} AND ac3.isProsecutor = true)) "
            + "AND ((COALESCE(:#{#req.citizenId}, 0) = 0) OR (cp.citizens.id = :#{#req.citizenId})) "
            + "AND ((COALESCE(:#{#req.statusId}, 0) = 0) OR cs.id = :#{#req.statusId}) "
            + "AND ((COALESCE(:#{#req.getTimeFromDate()}, NULL) IS NULL) OR c.updatedAt >= :#{#req.getTimeFromDate()}) "
            + "AND ((COALESCE(:#{#req.getTimeToDate()}, NULL) IS NULL) OR c.updatedAt < :#{#req.getTimeToDate()}) "
            + "group by c.id, c.code, c.name, d.name, cs.name, c.updatedAt, ac.hasAccess")
    Page<AccountCaseResponse> getAccountCaseByLeadRole(
            @Param("accountId") Long accountId,
            @Param("hasAccess") Boolean hasAccess,
            @Param("departmentId") Long departmentId,
            AccountCaseRequest req,
            Pageable pageable);

    @Transactional
    @Query("SELECT new vn.eledevo.vksbe.dto.response.account.AccountCaseResponse "
            + "(c.id, c.code, c.name, d.name, cs.name, ac.hasAccess, c.updatedAt) "
            + "FROM Cases c "
            + "LEFT JOIN AccountCase ac ON ac.cases.id = c.id "
            + "LEFT JOIN AccountCase ac2 ON ac2.cases.id = c.id "
            + "LEFT JOIN AccountCase ac3 ON ac3.cases.id = c.id "
            + "LEFT JOIN CasePerson cp ON cp.cases.id = c.id "
            + "LEFT JOIN Departments d ON c.departments.id = d.id "
            + "LEFT JOIN CaseStatus cs ON cs.id = c.case_status.id "
            + "WHERE ac.accounts.id = :accountId "
            + "AND d.id = :departmentId "
            + "AND ((COALESCE(:hasAccess, NULL) IS NULL) OR ac.hasAccess = :hasAccess) "
            + "AND (COALESCE(:#{#req.textSearch}, '') = '' OR LOWER(c.name) LIKE %:#{#req.textSearch}% OR LOWER(c.code) LIKE %:#{#req.textSearch}%) "
            + "AND ((COALESCE(:#{#req.userInChargeId}, 0) = 0) OR (ac2.accounts.id = :#{#req.userInChargeId} AND ac2.isInCharge = true)) "
            + "AND ((COALESCE(:#{#req.prosecutorId}, 0) = 0) OR (ac3.accounts.id = :#{#req.prosecutorId} AND ac3.isProsecutor = true)) "
            + "AND ((COALESCE(:#{#req.citizenId}, 0) = 0) OR (cp.citizens.id = :#{#req.citizenId})) "
            + "AND ((COALESCE(:#{#req.statusId}, 0) = 0) OR cs.id = :#{#req.statusId}) "
            + "AND ((COALESCE(:#{#req.getTimeFromDate()}, NULL) IS NULL) OR c.updatedAt >= :#{#req.getTimeFromDate()}) "
            + "AND ((COALESCE(:#{#req.getTimeToDate()}, NULL) IS NULL) OR c.updatedAt < :#{#req.getTimeToDate()}) "
            + "group by c.id, c.code, c.name, d.name, cs.name, c.updatedAt, ac.hasAccess")
    Page<AccountCaseResponse> getAccountCaseBySubLeadRole(
            @Param("accountId") Long accountId,
            @Param("hasAccess") Boolean hasAccess,
            @Param("departmentId") Long departmentId,
            AccountCaseRequest req,
            Pageable pageable);

    @Transactional
    @Query("SELECT new vn.eledevo.vksbe.dto.response.account.AccountCaseResponse "
            + "(c.id, c.code, c.name, d.name, cs.name, ac.hasAccess, c.updatedAt) "
            + "FROM Cases c "
            + "LEFT JOIN AccountCase ac ON ac.cases.id = c.id "
            + "LEFT JOIN CasePerson cp ON cp.cases.id = c.id "
            + "LEFT JOIN Departments d ON c.departments.id = d.id "
            + "LEFT JOIN CaseStatus cs ON cs.id = c.case_status.id "
            + "WHERE ac.accounts.id = :accountId AND ac.hasAccess = true "
            + "AND d.id = :departmentId "
            + "group by c.id, c.code, c.name, d.name, cs.name, c.updatedAt, ac.hasAccess")
    Page<AccountCaseResponse> getAccountCase(
            @Param("accountId") Long accountId, @Param("departmentId") Long departmentId, Pageable pageable);

    @Query("SELECT p.uriName " + "FROM Profiles p "
            + "JOIN Accounts a ON p.accounts.id = a.id "
            + "JOIN AccountCase ac ON ac.accounts.id = a.id "
            + "WHERE ac.cases.id = :caseId AND p.uriName IS NOT NULL AND p.uriName <> ''")
    List<String> getUriNameOfLead(@Param("caseId") Long caseId);

    @Query(value = "SELECT role_id FROM accounts WHERE username = :username", nativeQuery = true)
    Long getRole(@Param("username") String username);

    @Query(value = "SELECT is_create_case FROM accounts WHERE department_id = :department_id", nativeQuery = true)
    Boolean getIsCreateCase(@Param("department_id") Long department_id);

    @Query(value = "SELECT department_id FROM accounts WHERE username = :username", nativeQuery = true)
    Long getDepartment(@Param("username") String username);

    @Query(value = "SELECT accounts.*, profiles.id AS profiles_id, profiles.avatar, profiles.full_name FROM accounts  INNER JOIN profiles  ON  accounts.id = profiles.account_id  WHERE department_id = :department_id", nativeQuery = true)
    Page getStakeHolderById(@Param("department_id") Long departmentId, Pageable pageable);

//    @Query(value = "SELECT * FROM roles WHERE role_id = :role_id", nativeQuery = true)
//    Page<Roles> getRolesById(@Param("role_id") Long roleId, Pageable pageable);
//
//    @Query(value = "SELECT * FROM profiles WHERE account_id = :account_id", nativeQuery = true)
//    Page<Profiles> getProfilesById(@Param("account_id") Long accountId, Pageable pageable);
}
