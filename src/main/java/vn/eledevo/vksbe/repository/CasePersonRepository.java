package vn.eledevo.vksbe.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.entity.CasePerson;

public interface CasePersonRepository extends BaseRepository<CasePerson, Long> {
    @Query(
            "SELECT cp FROM CasePerson cp WHERE cp.type in :type AND cp.cases.id = :id AND cp.citizens.id IN :isCheckTrue")
    List<CasePerson> findExistingCasePersons(List<String> type, Long id, List<Long> isCheckTrue);

    @Query(
            "SELECT cp FROM CasePerson cp WHERE cp.cases.id = :id AND cp.citizens.id IN :isCheckTrue and cp.type = :type")
    List<CasePerson> getSuspectAndDefendant(Long id, List<Long> isCheckTrue, String type);

    @Modifying
    @Transactional
    @Query(
            "DELETE FROM CasePerson cp WHERE cp.cases.id = :id AND cp.citizens.id IN :isCheckFalse  AND cp.type in :type")
    void deleteCitizenInCase(
            @Param("id") Long id, @Param("isCheckFalse") List<Long> isCheckFalse, @Param("type") List<String> type);
}
