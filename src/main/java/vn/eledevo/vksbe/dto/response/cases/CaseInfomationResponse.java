package vn.eledevo.vksbe.dto.response.cases;

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
public class CaseInfomationResponse {
    Long id;
    String name;
    String code;
    Long departmentId;
    String departmentName;
    String statusName;
    Long statusId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

    String type;
    String description;
    String createdBy;
    Boolean hasPermissionDownload; // Quyền download vụ án của user đang đăng nhập

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDateTime actualTime;
}
