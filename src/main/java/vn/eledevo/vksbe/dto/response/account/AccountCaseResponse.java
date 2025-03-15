package vn.eledevo.vksbe.dto.response.account;

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
public class AccountCaseResponse {
    Long id;
    String code;
    String name;
    String departmentName;
    String statusName;
    Boolean hasAccess;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

    public AccountCaseResponse(
            Long id,
            String code,
            String name,
            String departmentName,
            String statusName,
            Boolean hasAccess,
            LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.departmentName = departmentName;
        this.statusName = statusName;
        this.hasAccess = hasAccess;
        this.updatedAt = updatedAt.toLocalDate();
    }
}
