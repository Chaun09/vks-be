package vn.eledevo.vksbe.config.security.auditing;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.eledevo.vksbe.entity.Accounts;

public class ApplicationAuditAware implements AuditorAware<Long> {

    /**
     * Lấy thông tin về người dùng hiện tại đang xác thực trong ứng dụng.
     *
     * @return Optional chứa mã nhận dạng (ID) của người dùng nếu đang xác thực,
     * ngược lại trả về Optional.empty().
     */
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu không có thông tin xác thực hoặc là xác thực ẩn danh
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        // Chuyển đổi thông tin xác thực về đối tượng User
        Accounts userPrincipal = (Accounts) authentication.getPrincipal();
        // Trả về mã nhận dạng (ID) của người dùng
        return Optional.ofNullable(userPrincipal.getId());
    }
}
