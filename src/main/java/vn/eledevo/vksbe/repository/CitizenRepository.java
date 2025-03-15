package vn.eledevo.vksbe.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse;
import vn.eledevo.vksbe.dto.response.citizen.CitizenResponse;
import vn.eledevo.vksbe.entity.Citizens;

@Repository
public interface CitizenRepository extends BaseRepository<Citizens, Long> {
    @Query(
            "SELECT new vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse(cp.citizens.id, cp.type, cp.citizens.name, cp.citizens.citizenId, cp.citizens.gender, cp.citizens.profileImage, cp.citizens.workingAddress,cp.citizens.address,cp.citizens.position) "
                    + "FROM CasePerson cp "
                    + "WHERE cp.cases.id = :caseId "
                    + "AND (cp.citizens.name LIKE %:textSearch% OR cp.citizens.citizenId LIKE %:textSearch%) "
                    + "AND cp.type IN :types ")
    Page<CitizenCaseResponse> searchAllInvestigatorByCaseId(
            @Param("caseId") Long caseId,
            @Param("textSearch") String textSearch,
            @Param("types") List<String> types,
            Pageable pageable);

    @Query("SELECT new vn.eledevo.vksbe.dto.response.citizen.CitizenResponse(c.id, c.name, c.profileImage, c.citizenId)"
            + " FROM Citizens c "
            + "WHERE ((COALESCE(:textSearch, NULL) IS NULL ) "
            + "OR LOWER(c.name) LIKE CONCAT('%', LOWER(:textSearch), '%')"
            + "OR c.citizenId LIKE %:textSearch% ) ")
    Page<CitizenResponse> getListCitizenByTextSearch(String textSearch, Pageable pageable);

    boolean existsByCitizenId(String citizenId);

    @Query(
            "SELECT DISTINCT new vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse(cp.citizens.id, cp.type, cp.citizens.name, cp.citizens.citizenId, cp.citizens.gender, cp.citizens.profileImage, cp.citizens.workingAddress,cp.citizens.address,cp.citizens.position, cp.citizens.uriName) "
                    + "FROM CasePerson cp "
                    + "WHERE cp.cases.id = :caseId "
                    + "AND cp.type = 'INVESTIGATOR' ")
    List<CitizenCaseResponse> getinvestigatorOfflineList(@Param("caseId") Long caseId);

    @Query("SELECT distinct c.uriName FROM Citizens c " + "JOIN CasePerson cp ON c.id = cp.citizens.id "
            + "WHERE cp.cases.id = :caseId AND c.uriName IS NOT NULL AND c.uriName <> ''")
    List<String> getUriNameOfCitizens(@Param("caseId") Long caseId);

    @Query(
            "SELECT DISTINCT new vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse(cp.citizens.id, cp.type, cp.citizens.name, cp.citizens.citizenId, cp.citizens.gender, cp.citizens.profileImage, cp.citizens.workingAddress,cp.citizens.address,cp.citizens.position,cp.citizens.uriName)"
                    + "FROM CasePerson cp "
                    + "WHERE cp.cases.id = :caseId "
                    + "AND (cp.type = 'SUSPECT' OR cp.type = 'DEFENDANT') ")
    List<CitizenCaseResponse> getSuspectDefendantOfflineList(@Param("caseId") Long caseId);
}
