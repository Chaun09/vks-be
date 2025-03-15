package vn.eledevo.vksbe.dto.response.document;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DocumentResponse {
    Long id;
    String name;
    String documentType;
    Long parentId;
    String type;
    Long size;
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime updatedAt;

    String createdBy;
    String updatedBy;
    String path;
    DocumentParentResponse parent;

    // Constructor nhận tất cả các tham số, bao gồm các trường cho DocumentParentResponse
    public DocumentResponse(
            Long id,
            String name,
            String documentType,
            Long parentId,
            String type,
            Long size,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdBy,
            String updatedBy,
            String path,
            Long parentIdForParent,
            String parentName,
            String parentType) {
        this.id = id;
        this.name = name;
        this.documentType = documentType;
        this.parentId = parentId;
        this.type = type;
        this.size = size;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.path = path;
        this.parent = new DocumentParentResponse(parentIdForParent, parentName, parentType);
    }
}
