package vn.eledevo.vksbe.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.response.ChunkInfo;
import vn.eledevo.vksbe.dto.response.FileChunkResponse;
import vn.eledevo.vksbe.dto.response.FileInfoResponse;
import vn.eledevo.vksbe.utils.minio.MinioService;

@RestController
@RequestMapping("/api/v1/private/minio")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Minio Store")
public class MinioController {
    MinioService minioService;

    //    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
    //        return minioService.uploadFileChunk(file);
    //    }

    // Endpoint để download file từ MinIO
    @GetMapping("/download/{fileName}")
    public void downloadFile(@PathVariable String fileName, HttpServletResponse response) throws Exception {
        InputStream inputStream = minioService.downloadFile(fileName);

        response.setContentType("application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        byte[] buffer = new byte[16384];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            response.getOutputStream().write(buffer, 0, bytesRead);
        }
        inputStream.close();
        response.flushBuffer();
    }

    // Endpoint để xóa file khỏi MinIO
    @DeleteMapping("/delete")
    public String deleteFile(@RequestBody String fileName) throws Exception {
        minioService.deleteFile(fileName);
        return "File deleted successfully: " + fileName;
    }

    @GetMapping("/fileInfo")
    public FileInfoResponse getFileInfo(@RequestParam("fileName") String fileName) {
        ChunkInfo chunkInfo = minioService.getTotalChunks(fileName);
        return new FileInfoResponse(
                fileName, chunkInfo.getTotalChunks(), chunkInfo.getChunkSize(), chunkInfo.getLastChunkSize());
    }

    @GetMapping("/downloadChunk")
    public void downloadFileChunk(
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkNumber") int chunkNumber,
            HttpServletResponse response) {
        try {
            // Lấy chunk và thông tin độ dài từ service
            FileChunkResponse chunkResponse = minioService.getFileChunk(fileName, chunkNumber);
            // Thiết lập các header HTTP
            response.setContentType("application/octet-stream");
            response.setContentLengthLong(chunkResponse.getContentLength());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            // Ghi dữ liệu chunk vào output stream
            try (InputStream chunkStream = chunkResponse.getInputStream()) {
                org.apache.commons.io.IOUtils.copy(chunkStream, response.getOutputStream());
            }
            // Flush buffer để đảm bảo dữ liệu được gửi đi
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload/chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("chunk") MultipartFile chunk) {
        try {
            minioService.uploadChunkToMinio(fileName, chunkNumber, chunk);

            if (chunkNumber == totalChunks - 1) {
                String urlFile = minioService.mergeChunksOnMinio(fileName, totalChunks);
                return ResponseEntity.ok(
                        Map.of("success", true, "message", "File uploaded and merged successfully", "url", urlFile));
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Chunk uploaded successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private Map<String, String> chat = new HashMap<>();

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody String codeRequest) {
        // Thay thế nội dung chat bằng tin nhắn mới
        chat.put("chat", codeRequest);
        // Trả về nội dung chat mới
        return chat;
    }

    @GetMapping("/get")
    public Map<String, String> get() {
        return chat;
    }
}
