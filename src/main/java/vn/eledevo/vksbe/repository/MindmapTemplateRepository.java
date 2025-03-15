package vn.eledevo.vksbe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import vn.eledevo.vksbe.entity.MindmapTemplate;

public interface MindmapTemplateRepository extends BaseRepository<MindmapTemplate, Long> {
    boolean existsByNameAndDepartments_IdAndIdNot(String name, Long departmentId, Long currentId);

    boolean existsByNameAndDepartments_Id(String name, Long departmentId);

    @Query("select m from MindmapTemplate m"
            + " where m.departments.id =:departmentId "
            + "And ((COALESCE(:textSearch, NULL) IS NULL OR m.name LIKE %:textSearch% )) ")
    Page<MindmapTemplate> getListMindmapTemplate(Long departmentId, String textSearch, Pageable pageable);

    boolean existsByUrl(String url);
}
