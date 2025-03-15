package vn.eledevo.vksbe.constant;

public class RegexPattern {

    private RegexPattern() {}

    public static final String PHONE_NUMBER = "^(0|\\+84)(2[0-9]|3[2-9]|5[6-9]|7[0-9]|8[1-9]|9[0-9])[0-9]{7,8}$";

    public static final String ACCOUNT_FULL_NAME = "^[A-Za-zÀ-ỹ\\s]{1,255}$";

    public static final String CASE_STATUS_NAME = "^[\\p{L}\\s]+$";

    public static final String MINDMAP_NAME = "^[a-zA-Z0-9\\sÀ-ỹà-ỹ]*$";

    public static final String USERNAME = "^[a-zA-Z0-9]+$";

    public static final String SPECIAL_CHARACTER_DETECTION_PATTERN = "^[A-Za-zÀ-ỹ0-9\\s]+$";

    public static final String CITIZEN = "^\\d{9}|\\d{12}$";
}
