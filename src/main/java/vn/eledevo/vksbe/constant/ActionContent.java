package vn.eledevo.vksbe.constant;

public class ActionContent {
    private ActionContent() {}

    public static final String CREATE_CASE = ActionType.CREATE.getDescription() + " vụ án";
    public static final String GET_ALL_MIND_MAP_TEMPLATES =
            ActionType.VIEW.getDescription() + " danh sách sơ đồ mẫu trong vụ án";
    public static final String GET_IN_CHARGE_LIST_IN_CASE = "Xem cán bộ và lãnh đạo phụ trách";
    public static final String GET_PROSECUTOR_LIST_IN_CASE = "Xem danh sách kiểm sát viên trong vụ án";
    public static final String VIEW_CASE_FLOW = "xem sơ đồ tư duy tổng quan vụ án";
    public static final String GET_CASE_FLOW_DETAIL = "Xem chi tiết sơ đồ vụ án";
    public static final String ADD_CASE_FLOW = ActionType.CREATE.getDescription() + " sơ đồ vụ án";
    public static final String UPDATE_CASE_FLOW = "Cập nhật sơ đồ vụ án";
    public static final String GET_CASE = "Xem danh sách vụ án";
    public static final String GET_CITIZEN = "Xem và tìm kiếm danh sách công dân trong hệ thống";
    public static final String UPDATE_CITIZEN = "Chỉnh sửa công dân trong hệ thống";
    public static final String CREATE_CITIZEN = "Tạo mới công dân vào hệ thống";
    public static final String CITIZEN = "Công dân";
    public static final String CREATE_CASE_STATUS = "Thêm mới trạng thái vụ án";
    public static final String GET_ACCOUNT_CASE_FILTER =
            "Xem, tìm kiếm danh sách lãnh đạo và kiểm sát viên trong bộ lọc";
    public static final String AUTHORIZATION_TO_CREATE_A_CASE = "Cấp quyền tạo vụ án";
    public static final String REMOVE_PERMISSION_CREATE_A_CASE = "Gỡ quyền tạo vụ án";
    public static final String GET_ORGANIZATION_LIST = "Xem danh sách đơn vị";
    public static final String ORGANIZATION = "Đơn vị";
    public static final String CREATE_ORGANIZATION = "Thêm mới đơn vị";
    public static final String UPDATE_ORGANIZATION = "Sửa đơn vị";
    public static final String GET_ORGANIZATION_DETAIL = "Xem chi tiết đơn vị";
    public static final String DELETE_ORGANIZATION = "Xóa đơn vị";
    public static final String GET_CASE_DETAIL = "Xem thông tin chi tiết vụ án";
    public static final String GET_INVESTIGATOR_OR_SUSPECT_DEFENDANT_IN_CASE =
            "Xem danh sách điều tra viên hoặc bị can/bị cáo";
    public static final String UPDATE_CASE = "Chỉnh sửa thông tin vụ án";
    public static final String UPDATE_PROSECUTOR_INCHARGE_IN_CASE =
            "Chỉnh sửa danh sách kiểm sát viên hoặc lãnh đạo phụ trách";
    public static final String UPDATE_INVESTIGATOR_IN_CASE = "Chỉnh sửa danh sách điều tra viên";
    public static final String UPDATE_SUSPECT_DEFENDANT_TYPE = "Chỉnh sửa quyền bị can hoặc bị cáo";
    public static final String UPDATE_SUSPECT_DEFENDANT_LIST = "Chỉnh sửa danh sách bị can, bị cáo";
    public static final String DOWNLOAD_CASE = "Tải xuống vụ án";
    public static final String GET_ACCOUNT_DONT_HAVE_PERMISSION_DOWNLOAD_IN_CASE =
            "Xem danh sách tài khoản không có quyền download trong vụ án";
    public static final String REMOVE_DOWNLOAD_PERMISSION_IN_CASE = "Gỡ quyền download trong vụ án";
    public static final String GRANT_DOWNLOAD_PERMISSION_TO_ACCOUNT_IN_CASE =
            "Cấp quyền download cho tài khoản trong vụ án";
    public static final String GET_ACCOUNT_HAVE_PERMISSION_DOWNLOAD_IN_CASE =
            "Xem danh sách tài khoản có quyền download trong vụ án";
    public static final String UPDATE_CASE_STATUS = "Chỉnh sửa trạng thái vụ án";
    public static final String GET_CASE_STATUS = "Xem và tìm kiếm trạng thái vụ án";
    public static final String CASE_STATUS = "Trạng thái vụ án";
    public static final String GET_CASE_STATUS_DETAIL = "Xem chi tiết trạng thái vụ án";
    public static final String DELETE_CASE_STATUS = "Xóa trạng thái vụ án";

    public static final String GET_LIST_DEPARTMENT = "Xem Danh sách phòng ban";

    public static final String UPDATE_DEPARTMENT = "Chỉnh sửa phòng ban";

    public static final String DEPARTMENT = "Phòng ban";

    public static final String RESET_PASSWORD = "Thay đổi mật khẩu tài khoản về mặc định";

    public static final String LOCK_ACCOUNT = "Khóa tài khoản ";

    public static final String CONNECT_ACCOUNT_WITH_COMPUTER = "Liên kết tài khoản với thiết bị";

    public static final String REMOVE_USB = "Gỡ kết USB với tài khoản";

    public static final String CREATE_USB_TOKEN_LINK_TO_ACCOUNT = "Tạo USB token liên kết với tài khoản";

    public static final String ACTIVE_ACCOUNT = "Kích hoạt tài khoản";

    public static final String SWAP_ACCOUNT = "Thay đổi trạng thái tài khoản";

    public static final String CREATE_ACCOUNT = "Tạo mới tài khoản";

    public static final String REMOVE_ACCOUNT_WITH_COMPUTER = "Gỡ tài khoản với thiết bị";

    public static final String UPDATE_ACCOUNT = "Chỉnh sửa thông tin tài khoản";

    public static final String UPDATE_AVATAR_ACCOUNT = "Chỉnh sửa ảnh tài khoản";

    public static final String UPDATE_PIN_ACCOUNT = "Thay đổi mã PIN tài khoản";

    public static final String CREATE_USB_TOKEN = "Tạo usb token";

    public static final String CREATE_MIND_MAP = "Tạo mới sơ đồ mẫu";

    public static final String UPDATE_MIND_MAP = "Cập nhật sơ đồ mẫu";

    public static final String DELETE_MIND_MAP = "Xóa sơ đồ mẫu";

    public static final String CREATE_PIN = "Tạo mã pin";

    public static final String CHANGE_PASSWORD_FIRST = "Đổi mật khẩu lần đầu";

    public static final String CREATE_COMPUTER = "Tạo mới thiết bị máy tính";

    public static final String UPDATE_COMPUTER = "Chỉnh sửa thiết bị máy tính";
}
