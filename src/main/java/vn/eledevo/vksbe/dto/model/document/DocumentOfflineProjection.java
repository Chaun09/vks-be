package vn.eledevo.vksbe.dto.model.document;

import java.time.LocalDateTime;

public interface DocumentOfflineProjection {
    Long getId();

    String getName();

    String getUriName();

    String getDocumentType();

    String getType();

    Long getSize();

    String getDescription();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getCreatedBy();

    String getUpdatedBy();

    String getPath();

    Long getParentId();

    String getChildIds();
}
