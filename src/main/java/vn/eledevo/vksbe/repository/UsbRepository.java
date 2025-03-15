package vn.eledevo.vksbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.eledevo.vksbe.dto.request.UsbRequest;
import vn.eledevo.vksbe.dto.response.usb.UsbConnectedResponse;
import vn.eledevo.vksbe.dto.response.usb.UsbResponseFilter;
import vn.eledevo.vksbe.entity.Usbs;

public interface UsbRepository extends BaseRepository<Usbs, Long> {
    Optional<Usbs> findByAccounts_Id(Long id);

    @Query(
            "SELECT new vn.eledevo.vksbe.dto.response.usb.UsbConnectedResponse(u.id, u.name, u.usbCode, u.createdAt, u.usbVendorCode) "
                    + "FROM Usbs u "
                    + "WHERE u.accounts.id = :accountId")
    List<UsbConnectedResponse> findUsbConnectedByAccountId(@Param("accountId") Long accountId);

    @Query(
            "SELECT new vn.eledevo.vksbe.dto.response.usb.UsbResponseFilter(u.id, u.name, u.usbCode, u.usbVendorCode, u.keyUsb, u.status, u.createdAt, u.updatedAt, u.createdBy, u.updatedBy, p.fullName) "
                    + "FROM Usbs u "
                    + "LEFT JOIN Accounts a ON a.id = u.accounts.id "
                    + "LEFT JOIN Profiles p ON a.id = p.accounts.id "
                    + "WHERE (COALESCE(u.usbCode, '') LIKE %:#{#usbRequest.usbCode}% OR :#{#usbRequest.usbCode} IS NULL) "
                    + "AND (COALESCE(p.fullName, '') LIKE %:#{#usbRequest.createByAccountName}% OR :#{#usbRequest.createByAccountName} IS NULL) "
                    + "AND (:#{#usbRequest.status}='' OR COALESCE(u.status, '') = :#{#usbRequest.status} OR :#{#usbRequest.status} IS NULL) "
                    + "AND DATE(u.createdAt) BETWEEN DATE(:#{#usbRequest.fromDate}) AND DATE(:#{#usbRequest.toDate})")
    Page<UsbResponseFilter> getUsbDeviceList(UsbRequest usbRequest, Pageable pageable);

    @Query(
            "select u from Usbs u inner join Accounts a on u.accounts.id = a.id where a.id =:accountId and u.status = 'CONNECTED'")
    Optional<Usbs> usbByAccountAndConnect(Long accountId);

    @Query("select u from Usbs u where u.usbCode =:usbCode and u.usbVendorCode =:usbVendorCode")
    Optional<Usbs> usbTokenNew(String usbCode, String usbVendorCode);

    Boolean existsByUsbCodeOrName(String usbCode, String name);

    Boolean existsByUsbCode(String usbCode);

    Boolean existsByName(String name);
}
