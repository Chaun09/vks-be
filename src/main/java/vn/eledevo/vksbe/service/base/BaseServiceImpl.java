package vn.eledevo.vksbe.service.base;

import java.util.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.mapper.BaseMapper;
import vn.eledevo.vksbe.repository.BaseRepository;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseServiceImpl<I, O, E, T> implements BaseService<I, O, T> {
    BaseMapper<I, O, E> mapper;

    BaseRepository<E, T> repository;

    protected BaseServiceImpl(BaseMapper<I, O, E> mapper, BaseRepository<E, T> repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    protected Map<String, String> toInsertErrors(I rq) {
        return new HashMap<>();
    }

    protected Map<String, String> toUpdateErrors(T id, I rq) {
        return new HashMap<>();
    }

    protected Map<String, String> toDeleteErrors(T id) {
        return new HashMap<>();
    }

    protected Map<String, String> toDeleteErrors(Set<T> ids) {
        return new HashMap<>();
    }

    @Override
    public O insert(I rq) throws ValidationException {
        Map<String, String> errors = toInsertErrors(rq);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        E t = mapper.toEntity(rq);
        E tResult = repository.save(t);
        return mapper.toResponse(tResult);
    }

    @Override
    public O update(T id, I rq) throws ValidationException, ApiException {
        E t = repository.findById(id).orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        Map<String, String> errors = toUpdateErrors(id, rq);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        E tUpdate = mapper.toEntityUpdate(rq, t);
        E tResult = repository.save(tUpdate);
        return mapper.toResponse(tResult);
    }

    @Override
    public O getById(T id) {
        Optional<E> tOptional = repository.findById(id);
        return mapper.toResponse(tOptional.orElse(null));
    }

    @Override
    public List<O> getByIds(Set<T> ids) {
        List<E> tList = repository.findAllById(ids);
        return mapper.toListResponse(tList);
    }

    @Override
    public O deleteById(T id) throws ValidationException {
        Map<String, String> errors = toDeleteErrors(id);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        repository.deleteById(id);
        return null;
    }

    @Override
    public List<O> deleteByIds(Set<T> ids) throws ValidationException {
        Map<String, String> errors = toDeleteErrors(ids);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        repository.deleteAllById(ids);
        return Collections.emptyList();
    }

    //    @Override
    //    public Rp softDeleteById(ID id) throws ValidationException {
    //        Map<String, String> errors = toDeleteErrors(id);
    //        if (!errors.isEmpty()) {
    //            throw new ValidationException(errors);
    //        }
    //        repository.softDeleteById(id);
    //        return null;
    //    }
}
