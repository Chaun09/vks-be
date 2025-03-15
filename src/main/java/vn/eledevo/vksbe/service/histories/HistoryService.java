package vn.eledevo.vksbe.service.histories;

import vn.eledevo.vksbe.constant.ObjectTableType;
import vn.eledevo.vksbe.dto.request.history.HistoryFilterRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.history.HistoryResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.exception.ApiException;

public interface HistoryService {
    void SaveHistory(
            Accounts accounts,
            String action,
            ObjectTableType objectTableType,
            Long objectId,
            String objectName,
            String iconType,
            Long caseId);

    ResponseFilter<HistoryResponse> getHistoryCase(HistoryFilterRequest request, Long caseId, Long page, Long pageSize)
            throws ApiException;

    ResponseFilter<HistoryResponse> getHistoryApp(HistoryFilterRequest request, Long page, Long pageSize)
            throws ApiException;
}
