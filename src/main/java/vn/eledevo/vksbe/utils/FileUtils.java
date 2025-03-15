package vn.eledevo.vksbe.utils;

import static vn.eledevo.vksbe.constant.FileConst.WHITE_LIST_EXTENSION_FILE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    private FileUtils() {}

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    public static String getContentType(String fileName) {
        String fileExtension = getFileExtension(fileName).toLowerCase();
        return switch (fileExtension) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    public static boolean isAllowedExtension(String fileExtension, String[] allowedExtensions) {
        return Arrays.asList(allowedExtensions).contains(fileExtension.toLowerCase());
    }

    public static boolean isPathAllowedExtension(String path, String[] allowedExtensions) {
        return Arrays.stream(allowedExtensions)
                .anyMatch(ext -> path.toLowerCase().endsWith(ext));
    }

    public static String generateUniqueFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        return UUID.randomUUID() + fileExtension;
    }

    public static boolean validateExtensionFileDocument(MultipartFile multipartFile) {
        String fileExtension = getFileExtension(multipartFile.getOriginalFilename());
        return Arrays.stream(WHITE_LIST_EXTENSION_FILE)
                .anyMatch(ext -> fileExtension.toLowerCase().endsWith(ext));
    }

    public static boolean validateExtensionFileDocumentChunk(String fileName) {
        String fileExtension = getFileExtension(fileName);
        return Arrays.stream(WHITE_LIST_EXTENSION_FILE)
                .anyMatch(ext -> fileExtension.toLowerCase().endsWith(ext));
    }

    public static String getFileTypeChunk(String fileName) {
        String fileExtension = getFileExtension(fileName);
        return switch (fileExtension) {
            case ".png", ".jpg", ".jpeg" -> "IMAGE";
            case ".pdf" -> "PDF";
            case ".mov", ".mp4" -> "VIDEO";
            case ".mp3" -> "AUDIO";
            default -> "UNKNOWN";
        };
    }

    public static String getFileType(MultipartFile multipartFile) {
        String fileExtension = getFileExtension(multipartFile.getOriginalFilename());
        return switch (fileExtension) {
            case ".png", ".jpg", ".jpeg" -> "IMAGE";
            case ".pdf" -> "PDF";
            case ".mov", ".mp4" -> "VIDEO";
            case ".mp3" -> "AUDIO";
            default -> "UNKNOWN";
        };
    }

    public static String getUriName(String url) {
        String fileName = "";
        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // Lấy phần đường dẫn từ URL
            fileName = path.substring(path.lastIndexOf('/') + 1); // Lấy tên tệp từ phần đường dẫn
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public static String getOriginalName(String url) {
        String originalName = "";
        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // Lấy phần đường dẫn từ URL
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            originalName = fileName.replaceFirst("^[^_]+_", "");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return originalName;
    }
}
