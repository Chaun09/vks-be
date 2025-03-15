package vn.eledevo.vksbe.mapper;

import java.util.Optional;

import vn.eledevo.vksbe.dto.response.document.DocumentParentResponse;
import vn.eledevo.vksbe.dto.response.document.DocumentResponse;
import vn.eledevo.vksbe.entity.Documents;

public class DocumentMapper {
    public static DocumentResponse toResponse(Documents documents) {
        DocumentParentResponse documentResponse = new DocumentParentResponse();
        if (documents.getParentId() != null) {
            documentResponse.setId(documents.getParentId().getId());
            documentResponse.setName(documents.getParentId().getName());
            documentResponse.setType(documents.getParentId().getType());
        }
        return DocumentResponse.builder()
                .name(documents.getName())
                .id(documents.getId())
                .documentType(documents.getDocumentType())
                .parentId(Optional.ofNullable(documents.getParentId())
                        .map(Documents::getId)
                        .orElse(null))
                .createdAt(documents.getCreatedAt())
                .updatedAt(documents.getUpdatedAt())
                .createdBy(documents.getCreatedBy())
                .updatedBy(documents.getUpdatedBy())
                .parent(documentResponse)
                .type(documents.getType())
                .path(Optional.ofNullable(documents.getPath()).orElse(""))
                .build();
    }
}
