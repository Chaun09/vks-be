package vn.eledevo.vksbe.dto.response.history;

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
public class HistoryResponse {
    Long id;
    Long staffId;
    String staffCode;
    String fullName;
    String action;
    String objectType;
    String objectName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime timestamp;

    String iconType;
}
