package vn.eledevo.vksbe.service.base;

import java.util.List;
import java.util.Set;

import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;

public interface BaseService<I, O, T> {
    O insert(I rq) throws ValidationException;

    O update(T id, I rq) throws ValidationException, ApiException;

    O getById(T id);

    List<O> getByIds(Set<T> ids);

    O deleteById(T id) throws ValidationException;

    List<O> deleteByIds(Set<T> ids) throws ValidationException;

    O softDeleteById(T id) throws ValidationException;
}
