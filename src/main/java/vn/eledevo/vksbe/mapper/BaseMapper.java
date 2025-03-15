package vn.eledevo.vksbe.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.mapstruct.*;

import vn.eledevo.vksbe.dto.response.PageResponse;

public abstract class BaseMapper<I, O, E> {
    public abstract E toEntity(I rq);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract E toEntityUpdate(I rq, @MappingTarget E e);

    public abstract O toResponse(E e);

    public abstract List<O> toListResponse(List<E> eList);

    public PageResponse<O> toPageResponse(List<E> eList, long total) {
        List<O> rpList = toListResponse(eList);
        return new PageResponse<>(total, rpList);
    }

    Long mapLocalDateToEpochTimestamp(LocalDate dateTime) {
        return dateTime != null ? dateTime.atStartOfDay().toEpochSecond(ZoneOffset.UTC) : null;
    }

    LocalDate mapEpochTimestampToLocalDate(Long epochTimestamp) {
        return epochTimestamp != null
                ? LocalDate.ofInstant(Instant.ofEpochSecond(epochTimestamp), ZoneOffset.UTC)
                : null;
    }
}
