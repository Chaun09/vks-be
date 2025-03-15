package vn.eledevo.vksbe.dto.response.case_flow;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseFlowResponse {
    Long id;
    String name;
    String url;
    String createdBy;
    String updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate createdAt;

    String uriName;
    String dataLink;
    String dataNode;

    public CaseFlowResponse(Long id) {
        this.id = id;
    }

    public CaseFlowResponse(Long id, String name, String dataLink, String dataNode) {
        this.id = id;
        this.name = name;
        this.dataLink = dataLink;
        this.dataNode = dataNode;
    }
}
