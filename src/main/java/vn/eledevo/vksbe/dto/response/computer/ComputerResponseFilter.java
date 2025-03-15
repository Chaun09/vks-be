package vn.eledevo.vksbe.dto.response.computer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComputerResponseFilter {
    Long id;
    String name;
    String accountFullName;
    String code;
    String status;
    String brand;
    String type;
    String note;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

    String createdBy;
    String updatedBy;

    public ComputerResponseFilter(
            Long id,
            String name,
            String accountFullName,
            String code,
            String status,
            String brand,
            String type,
            String note,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdBy,
            String updatedBy) {
        this.id = id;
        this.name = name;
        this.accountFullName = accountFullName;
        this.code = code;
        this.status = status;
        this.brand = brand;
        this.type = type;
        this.note = note;
        this.createdAt = createdAt.toLocalDate();
        this.updatedAt = updatedAt.toLocalDate();
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
