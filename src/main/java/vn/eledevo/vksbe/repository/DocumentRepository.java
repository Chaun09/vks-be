package vn.eledevo.vksbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.dto.model.document.DocumentOfflineProjection;
import vn.eledevo.vksbe.dto.response.document.DocumentParentResponse;
import vn.eledevo.vksbe.dto.response.document.DocumentResponse;
import vn.eledevo.vksbe.entity.Documents;

public interface DocumentRepository extends BaseRepository<Documents, Long> {
    List<Documents> findByParentId(Documents documentsParent);

    @Query(
            "select new vn.eledevo.vksbe.dto.response.document.DocumentResponse (dc.id, dc.name, dc.documentType, dc.parentId.id, dc.type, dc.size, dc.description, dc.createdAt, dc.updatedAt, dc.createdBy, dc.updatedBy, dc.path, dc.parentId.id, dc.parentId.name, dc.parentId.documentType) "
                    + "from Documents dc where dc.cases.id = :caseId "
                    + "and (coalesce(:textSearch, null) is null or dc.name like concat('%', :textSearch, '%')) "
                    + "and (coalesce(:type, null) is null or dc.documentType = :type) "
                    + "and  (coalesce(:documentId, null) is null or dc.parentId.id = :documentId) ")
    Page<DocumentResponse> getDocument(Long caseId, String textSearch, String type, Long documentId, Pageable pageable);

    Page<Documents> findByIsDefault(Boolean isDefault, Pageable pageable);

    Optional<Documents> findFirstByDocumentTypeAndIsDefaultTrue(String type);

    @Query("select new vn.eledevo.vksbe.dto.response.document.DocumentParentResponse (dc.id, dc.name, dc.documentType) "
            + "from Documents dc where dc.documentType = :type and dc.isDefault = true ")
    Optional<DocumentParentResponse> findByDocDefault(String type);

    @Query("select new vn.eledevo.vksbe.dto.response.document.DocumentParentResponse (dc.id, dc.name, dc.documentType) "
            + "from Documents dc where dc.id = :id")
    Optional<DocumentParentResponse> findByIdDoc(Long id);

    Optional<Documents> findByIdAndIsDefaultTrue(Long id);

    @Query(
            value = "SELECT d.id, d.name, d.uri_name AS uriName, d.document_type AS documentType, "
                    + "d.type, d.size, d.description, d.created_at AS createdAt, d.updated_at AS updatedAt, "
                    + "d.created_by AS createdBy, d.updated_by AS updatedBy, d.path, "
                    + "d.parent_id AS parentId, "
                    + "GROUP_CONCAT(child.id) AS childIds "
                    + "FROM documents d "
                    + "LEFT JOIN documents child ON child.parent_id = d.id "
                    + "WHERE d.document_type IN ('TRIAL', 'INVESTIGATION') "
                    + "AND d.case_id = :caseId "
                    + "GROUP BY d.id, d.name, d.uri_name, d.document_type, d.type, d.size, d.description, "
                    + "d.created_at, d.updated_at, d.created_by, d.updated_by, d.path, d.parent_id "
                    + "ORDER BY "
                    + "CASE WHEN d.type = 'FOLDER' THEN 1 ELSE 2 END ASC, "
                    + "updatedAt DESC",
            nativeQuery = true)
    List<DocumentOfflineProjection> getOfflineDocuments(@Param("caseId") Long caseId);

    @Query(
            "SELECT d.uriName FROM Documents d where d.cases.id = :caseId AND d.uriName IS NOT NULL AND (d.uriName <> '' OR d.parentId.id = 3L)")
    List<String> getUriNameOfDocuments(@Param("caseId") Long caseId);

    @Query("SELECT d.id " + "FROM Documents d "
            + "WHERE d.parentId.id = :parentId "
            + "AND d.cases.id = :caseId "
            + "ORDER BY "
            + "CASE WHEN d.type = 'FOLDER' THEN 1 ELSE 2 END ASC, "
            + "d.updatedAt DESC")
    List<Long> getChildrenIds(@Param("parentId") Long parentId, @Param("caseId") Long caseId);
}
