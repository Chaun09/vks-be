package vn.eledevo.vksbe.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import vn.eledevo.vksbe.dto.response.history.HistoryResponse;
import vn.eledevo.vksbe.entity.Histories;

public interface HistoryRepository extends BaseRepository<Histories, Long> {
    @Query(
            "select new vn.eledevo.vksbe.dto.response.history.HistoryResponse (h.id, h.staffId, h.staffCode, h.fullName, h.action, h.objectType, h.objectName, h.timestamp, h.iconType) "
                    + "from Histories h where 1=1 "
                    + "and (coalesce(:caseId , null) is null or h.caseId = :caseId) "
                    + "and ( (coalesce(:textSearch , null)) is null "
                    + "or (LOWER(h.fullName) LIKE LOWER(CONCAT('%', :textSearch, '%'))) "
                    + "or (LOWER(h.staffCode) LIKE LOWER(CONCAT('%', :textSearch, '%')))"
                    + ") "
                    + "and (coalesce(:fromDate , null) is null or h.timestamp >=  :fromDate) "
                    + "and (coalesce(:toDate , null) is null or h.timestamp <  :toDate)"
                    + " ")
    Page<HistoryResponse> searchHistoryCase(
            String textSearch, Long caseId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query(
            "select new vn.eledevo.vksbe.dto.response.history.HistoryResponse (h.id, h.staffId, h.staffCode, h.fullName, h.action, h.objectType, h.objectName, h.timestamp, h.iconType) "
                    + "from Histories h where 1=1 "
                    + "and ( (coalesce(:textSearch , null)) is null "
                    + "or (LOWER(h.fullName) LIKE LOWER(CONCAT('%', :textSearch, '%'))) "
                    + "or (LOWER(h.staffCode) LIKE LOWER(CONCAT('%', :textSearch, '%')))"
                    + ") "
                    + "and (coalesce(:fromDate , null) is null or h.timestamp >=  :fromDate) "
                    + "and (coalesce(:toDate , null) is null or h.timestamp <  :toDate)"
                    + " ")
    Page<HistoryResponse> searchHistoryApp(
            String textSearch, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
