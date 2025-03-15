package vn.eledevo.vksbe.dto.response.case_offline;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.response.account.AccountFilterCaseResponse;
import vn.eledevo.vksbe.dto.response.case_flow.CaseFlowResponse;
import vn.eledevo.vksbe.dto.response.cases.CaseInfomationResponse;
import vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseOverviewResponse {
    CaseInfomationResponse caseInfo;
    List<AccountFilterCaseResponse> userInchargeList;
    List<AccountFilterCaseResponse> prosecutorList;
    List<CitizenCaseResponse> investigatorList;
    List<CitizenCaseResponse> suspectDefendantList;
    CaseFlowResponse caseFlow;
}
