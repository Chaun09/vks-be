package vn.eledevo.vksbe.dto.response.organization;

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
public class OrganizationResponse {
    Long id;
    String name;
    String code;
    String address;
    Boolean isDefault;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

    public OrganizationResponse(
            Long id, String name, String code, String address, Boolean isDefault, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.address = address;
        this.isDefault = isDefault;
        this.updatedAt = updatedAt.toLocalDate();
    }
}
