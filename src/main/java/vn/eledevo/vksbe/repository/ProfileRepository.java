package vn.eledevo.vksbe.repository;

import vn.eledevo.vksbe.entity.Profiles;

public interface ProfileRepository extends BaseRepository<Profiles, Long> {
    Profiles findByAccounts_Id(Long accountId);
}
