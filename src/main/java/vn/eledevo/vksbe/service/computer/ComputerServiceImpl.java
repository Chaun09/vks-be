package vn.eledevo.vksbe.service.computer;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.ActionContent;
import vn.eledevo.vksbe.constant.ErrorCodes.ComputerErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.constant.IconType;
import vn.eledevo.vksbe.constant.ObjectTableType;
import vn.eledevo.vksbe.constant.ResponseMessage;
import vn.eledevo.vksbe.dto.model.computer.ComputersModel;
import vn.eledevo.vksbe.dto.request.ComputerRequest;
import vn.eledevo.vksbe.dto.request.computer.ComputerRequestForCreate;
import vn.eledevo.vksbe.dto.request.computer.ComputerToCheckExist;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.ResultList;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponse;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponseFilter;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Computers;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.mapper.ComputerMapper;
import vn.eledevo.vksbe.repository.ComputerRepository;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComputerServiceImpl implements ComputerService {
    ComputerRepository computerRepository;
    ComputerMapper computerMapper;
    HistoryService historyService;

    @Override
    public ResponseFilter<ComputerResponseFilter> getComputerList(
            ComputerRequest computerRequest, Integer currentPage, Integer limit) {
        Pageable pageable =
                PageRequest.of(currentPage - 1, limit, Sort.by("updatedAt").descending());
        Page<ComputerResponseFilter> page = computerRepository.getComputerList(computerRequest, pageable);
        return new ResponseFilter<>(
                page.getContent(),
                (int) page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                page.getTotalPages());
    }

    @Override
    public HashMap<String, String> updateComputer(Long requestId, ComputersModel computerRequest)
            throws ApiException, ValidationException {
        Accounts loginAccount = SecurityUtils.getUser();
        Map<String, String> errors = new HashMap<>();
        Computers computer =
                computerRepository.findById(requestId).orElseThrow(() -> new ApiException(ComputerErrorCode.NOT_FOUND));
        Computers computerByName = computerRepository
                .findByNameAndIdNot(computerRequest.getName(), requestId)
                .orElse(null);
        if (computerByName != null) {
            errors.put("name", ResponseMessage.PC_NAME_ALREADY_EXISTS);
            throw new ValidationException(errors);
        }

        computer.setName(computerRequest.getName());
        computer.setBrand(computerRequest.getBrand());
        computer.setType(computerRequest.getType());
        computer.setNote(computerRequest.getNote());
        computerRepository.save(computer);
        historyService.SaveHistory(
                loginAccount,
                ActionContent.UPDATE_COMPUTER,
                ObjectTableType.COMPUTER,
                computer.getId(),
                computer.getName(),
                IconType.COMPUTER.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public ResultList<ComputerResponse> getDisconnectedComputers(String textSearch) {
        String keyword =
                StringUtils.isBlank(textSearch) ? null : textSearch.trim().toLowerCase();
        List<Computers> computersList = computerRepository.getByTextSearchAndAccountsIsNull(keyword);
        return new ResultList<>(computerMapper.toListResponse(computersList));
    }

    @Override
    @Transactional
    public HashMap<String, String> createComputer(ComputerRequestForCreate request)
            throws ApiException, ValidationException {
        Accounts loginAccount = SecurityUtils.getUser();
        Map<String, String> errors = new HashMap<>();
        Boolean computerExist = computerRepository.existsByCode(request.getCode());
        if (Objects.equals(computerExist, Boolean.TRUE)) {
            errors.put("code", ComputerErrorCode.PC_CODE_ALREADY_EXISTS.getMessage());
            throw new ValidationException(errors);
        }
        if (computerRepository.existsByName(request.getName())) {
            errors.put("name", ResponseMessage.PC_NAME_ALREADY_EXISTS);
            throw new ValidationException(errors);
        }

        Computers computersCreate = computerRepository.save(computerMapper.toResource(request));
        computerMapper.toResponse(computersCreate);
        historyService.SaveHistory(
                loginAccount,
                ActionContent.CREATE_COMPUTER,
                ObjectTableType.COMPUTER,
                computersCreate.getId(),
                computersCreate.getName(),
                IconType.COMPUTER.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public HashMap<String, String> checkExistComputer(ComputerToCheckExist computer) throws ApiException {
        if (computer.getComputerCode() == null || computer.getComputerCode().isBlank()) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
        Optional<Computers> result = computerRepository.findComputersByCode(computer.getComputerCode());
        if (result.isPresent()) {
            throw new ApiException(ComputerErrorCode.PC_CODE_ALREADY_EXISTS);
        }
        return new HashMap<>();
    }
}
