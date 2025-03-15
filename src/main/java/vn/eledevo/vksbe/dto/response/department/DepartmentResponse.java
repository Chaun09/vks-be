package vn.eledevo.vksbe.dto.response.department;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentResponse {
    Long id;
    String name;
    String code;
    String leader;
    String organizationName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

    public DepartmentResponse(
            Long id,
            String name,
            String code,
            String leader,
            String organizationName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.leader = leader;
        this.organizationName = organizationName;
        this.createdAt = createdAt.toLocalDate();
        this.updatedAt = updatedAt.toLocalDate();
    }
}
