package vn.eledevo.vksbe.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import vn.eledevo.vksbe.dto.response.case_status.CaseStatusResponse;
import vn.eledevo.vksbe.entity.CaseStatus;

@Component
public class CaseStatusMapper {
    // Private constructor
    private CaseStatusMapper() {}

    public static CaseStatusResponse toResponse(CaseStatus e) {
        if (Objects.isNull(e)) {
            return new CaseStatusResponse();
        }
        return CaseStatusResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt().toLocalDate())
                .updatedAt(e.getUpdatedAt().toLocalDate())
                .isDefault(false)
                .build();
    }

    public List<CaseStatusResponse> toListResponse(List<CaseStatus> eList) {
        if (CollectionUtils.isEmpty(eList)) {
            return Collections.emptyList();
        }

        List<CaseStatusResponse> list = new ArrayList<>(eList.size());
        for (CaseStatus caseStatus : eList) {
            list.add(toResponse(caseStatus));
        }
        return list;
    }
}
