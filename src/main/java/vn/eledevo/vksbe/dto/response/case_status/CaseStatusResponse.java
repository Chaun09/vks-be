package vn.eledevo.vksbe.dto.response.case_status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseStatusResponse {
    Long id;
    String name;
    String description;
    String createdBy;
    String updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

    Boolean isDefault;

    public CaseStatusResponse(
            Long id,
            String name,
            String description,
            String updatedBy,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.updatedBy = updatedBy;
        this.createdBy = createdBy;
        this.createdAt = createdAt.toLocalDate();
        this.updatedAt = updatedAt.toLocalDate();
        this.isDefault = isDefault;
    }
}
