package vn.eledevo.vksbe.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import vn.eledevo.vksbe.dto.request.computer.ComputerRequestForCreate;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponse;
import vn.eledevo.vksbe.entity.Computers;

@Component
public class ComputerMapper {

    public Computers toResource(ComputerRequestForCreate request) {
        if (Objects.isNull(request)) {
            return null;
        }
        Computers computers = new Computers();
        computers.setName(request.getName());
        computers.setCode(request.getCode());
        computers.setBrand(request.getBrand());
        computers.setType(request.getType());
        computers.setNote(request.getNote());
        return computers;
    }

    public ComputerResponse toResponse(Computers e) {
        if (Objects.isNull(e)) {
            return null;
        }

        ComputerResponse.ComputerResponseBuilder computerResponse = ComputerResponse.builder();

        computerResponse.id(e.getId());
        computerResponse.name(e.getName());
        computerResponse.code(e.getCode());
        computerResponse.status(e.getStatus());
        computerResponse.brand(e.getBrand());
        computerResponse.type(e.getType());
        computerResponse.note(e.getNote());
        computerResponse.createdAt(e.getCreatedAt().toLocalDate());
        computerResponse.updatedAt(e.getUpdatedAt().toLocalDate());
        computerResponse.createdBy(e.getCreatedBy());
        computerResponse.updatedBy(e.getUpdatedBy());

        return computerResponse.build();
    }

    public List<ComputerResponse> toListResponse(List<Computers> eList) {
        if (CollectionUtils.isEmpty(eList)) {
            return Collections.emptyList();
        }

        List<ComputerResponse> list = new ArrayList<>(eList.size());
        for (Computers computers : eList) {
            list.add(toResponse(computers));
        }

        return list;
    }
}
