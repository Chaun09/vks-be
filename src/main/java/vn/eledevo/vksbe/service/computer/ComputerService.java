package vn.eledevo.vksbe.service.computer;

import java.util.HashMap;

import vn.eledevo.vksbe.dto.model.computer.ComputersModel;
import vn.eledevo.vksbe.dto.request.ComputerRequest;
import vn.eledevo.vksbe.dto.request.computer.ComputerRequestForCreate;
import vn.eledevo.vksbe.dto.request.computer.ComputerToCheckExist;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.ResultList;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponse;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponseFilter;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;

public interface ComputerService {

    ResponseFilter<ComputerResponseFilter> getComputerList(
            ComputerRequest computerRequest, Integer page, Integer pageSize) throws ApiException;

    HashMap<String, String> updateComputer(Long computerId, ComputersModel computerRequest)
            throws ApiException, ValidationException;

    ResultList<ComputerResponse> getDisconnectedComputers(String textSearch);

    HashMap<String, String> createComputer(ComputerRequestForCreate request) throws ApiException, ValidationException;

    HashMap<String, String> checkExistComputer(ComputerToCheckExist computerCode) throws ApiException;
}
