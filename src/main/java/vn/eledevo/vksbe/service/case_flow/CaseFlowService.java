package vn.eledevo.vksbe.service.case_flow;

import java.util.HashMap;

import org.springframework.web.multipart.MultipartFile;

import vn.eledevo.vksbe.dto.request.case_flow.CaseFlowCreateRequest;
import vn.eledevo.vksbe.dto.request.case_flow.CaseFlowUpdateRequest;
import vn.eledevo.vksbe.dto.response.ResultUrl;
import vn.eledevo.vksbe.dto.response.case_flow.CaseFlowResponse;
import vn.eledevo.vksbe.exception.ApiException;

public interface CaseFlowService {
    CaseFlowResponse getCaseFlow(Long caseId) throws ApiException;

    CaseFlowResponse addCaseFlow(Long caseId, CaseFlowCreateRequest caseFlowCreateRequest) throws ApiException;

    HashMap<String, String> updateCaseFlow(Long id, Long idCaseFlow, CaseFlowUpdateRequest caseFlowUpdateRequest)
            throws Exception;

    CaseFlowResponse getDetailCaseFlow(Long caseId, Long id) throws ApiException;

    ResultUrl uploadImg(MultipartFile file, Long id) throws Exception;
}
