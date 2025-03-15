package vn.eledevo.vksbe.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.dto.response.case_status.CaseStatusResponse;
import vn.eledevo.vksbe.entity.CaseStatus;

public interface CaseStatusRepository extends BaseRepository<CaseStatus, Long> {
    @Query(
            "SELECT new vn.eledevo.vksbe.dto.response.case_status.CaseStatusResponse(c.id, c.name, c.description, c.updatedBy, c.createdBy, c.createdAt, c.updatedAt, c.isDefault) "
                    + "FROM CaseStatus c "
                    + "WHERE ((COALESCE(:name, NULL) IS NULL) OR LOWER(c.name) LIKE CONCAT('%', LOWER(:name), '%')) "
                    + "AND (:fromDate IS NULL OR c.updatedAt >= :fromDate) "
                    + "AND (:toDate IS NULL OR c.updatedAt < :toDate)")
    Page<CaseStatusResponse> getCaseStatus(
            @Param("name") String name,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    Boolean existsByName(String name);

    Boolean existsByNameAndIdNot(String name, Long id);
}
