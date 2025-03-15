package vn.eledevo.vksbe.constant;

public class FileConst {

    private FileConst() {}

    public static final long BYTES_IN_MB = 1024L * 1024L;

    public static final long MAX_AVATAR_SIZE = 3L; // 10MB

    public static final String[] AVATAR_ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg"};

    public static final long MAX_IMG_MIND_MAP_SIZE = 3L; // 10MB

    public static String[] WHITE_LIST_EXTENSION_FILE = {"png", "jpg", ".jpeg", ".pdf", ".mov", ".mp4", ".mp3"};
}
