package vn.eledevo.vksbe.dto.response.case_offline;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentOfflineResponse {
    Long id;
    String name;
    String uriName;
    String documentType;
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
    Long parentId;
    List<Long> childIds;
}
