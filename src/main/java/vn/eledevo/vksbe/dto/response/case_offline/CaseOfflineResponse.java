package vn.eledevo.vksbe.dto.response.case_offline;

import java.util.HashMap;
import java.util.LinkedHashMap;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseOfflineResponse {
    CaseOverviewResponse caseOverviewResponse;
    LinkedHashMap<Long, DocumentOfflineResponse> trialDocuments;
    LinkedHashMap<Long, DocumentOfflineResponse> investigatedDocuments;
    HashMap<String, DocumentOfflineResponse> defaultDocuments;
}
