package vn.eledevo.vksbe.utils.minio;

import java.io.InputStream;
import java.net.URI;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.eledevo.vksbe.dto.response.ChunkInfo;
import vn.eledevo.vksbe.dto.response.FileChunkResponse;
import vn.eledevo.vksbe.utils.FileUtils;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioService {
    MinioClient minioClient;
    MinioProperties minioProperties;
    int CHUNK_SIZE = 200 * 1024 * 1024;
    int CHUNK_SIZE_UPLOAD = 128 * 1024 * 1024;

    public MinioService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;

        // Tạo bucket tự động nếu chưa tồn tại
        try {
            String bucketName = minioProperties.getBucketName();

            // Kiểm tra bucket có tồn tại không
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build())) {
                // Tạo bucket nếu chưa tồn tại
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket " + bucketName + " created successfully.");

                // Áp dụng chính sách public cho bucket
                setBucketPolicyPublic(bucketName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thiết lập bucket thành public
     *
     * @param bucketName Tên bucket cần đặt chính sách
     */
    private void setBucketPolicyPublic(String bucketName) {
        String policy =
                """
				{
					"Version": "2012-10-17",
					"Statement": [
						{
							"Effect": "Allow",
							"Principal": "*",
							"Action": [
								"s3:GetObject"
							],
							"Resource": [
								"arn:aws:s3:::%s/*"
							]
						}
					]
				}
				"""
                        .formatted(bucketName);

        try {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build());
            System.out.println("Bucket " + bucketName + " is now public.");
        } catch (Exception e) {
            System.err.println("Error setting public policy for bucket " + bucketName + ": " + e.getMessage());
        }
    }

    private String generateFileUrl(String fileName) {
        String endpoint = minioProperties.getUrl(); // URL MinIO server
        String bucketName = minioProperties.getBucketName(); // Tên bucket từ MinioProperties
        return String.format("%s/%s/%s", endpoint, bucketName, fileName);
    }

    public String uploadFile(MultipartFile file) throws Exception {
        FileUtils.validateExtensionFileDocument(file);
        String fileType = Objects.requireNonNull(file.getContentType()).split("/")[1];
        String fileName = UUID.randomUUID() + "." + fileType;
        minioClient.putObject(PutObjectArgs.builder().bucket(minioProperties.getBucketName()).object(fileName).stream(
                        file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        return generateFileUrl(fileName);
    }

    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(fileName)
                .build());
    }

    // Xóa file khỏi MinIO
    public void deleteFile(String urlImage) throws Exception {
        URI uri = new URI(urlImage);
        String path = uri.getPath(); // Lấy path từ URL
        String fileNameImage = path.substring(path.lastIndexOf('/') + 1); // Trích xuất tên file từ path
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(fileNameImage)
                .build());
    }

    //    public String uploadFileChunk(MultipartFile file) {
    //        String fileType = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
    //        String fileName = UUID.randomUUID() + "." + fileType; // Tạo objectName là fileName duy nhất
    //        String bucketName = minioProperties.getBucketName();
    //        List<String> chunkNames = new ArrayList<>(); // Lưu danh sách các chunk đã upload để xóa nếu cần
    //
    //        try (InputStream inputStream = file.getInputStream()) {
    //            long fileSize = file.getSize();
    //            if (fileSize <= CHUNK_SIZE_UPLOAD) {
    //                minioClient.putObject(PutObjectArgs.builder()
    //                        .bucket(bucketName)
    //                        .object(fileName)
    //                        .stream(inputStream, fileSize, -1)
    //                        .contentType(file.getContentType())
    //                        .build());
    //                return generateFileUrl(fileName);
    //            }
    //            int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE_UPLOAD);
    //            List<ComposeSource> sources = new ArrayList<>();
    //
    //            // Upload từng chunk
    //            for (int chunkNumber = 0; chunkNumber < totalChunks; chunkNumber++) {
    //                String chunkName = fileName + "_chunk_" + chunkNumber;
    //                chunkNames.add(chunkName); // Thêm chunk vào danh sách đã upload
    //
    //                byte[] chunkData = new byte[CHUNK_SIZE_UPLOAD];
    //                int bytesRead = inputStream.read(chunkData, 0, CHUNK_SIZE_UPLOAD);
    //
    //                // Upload từng chunk vào MinIO
    //                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(chunkName).stream(
    //                                new ByteArrayInputStream(chunkData, 0, bytesRead), bytesRead, -1)
    //                        .contentType(file.getContentType())
    //                        .build());
    //
    //                // Thêm mỗi chunk đã upload vào danh sách nguồn để hợp nhất sau này
    //                sources.add(ComposeSource.builder()
    //                        .bucket(bucketName)
    //                        .object(chunkName)
    //                        .build());
    //            }
    //
    //            // Hợp nhất tất cả các chunk thành một file hoàn chỉnh
    //            minioClient.composeObject(ComposeObjectArgs.builder()
    //                    .bucket(bucketName)
    //                    .object(fileName)
    //                    .sources(sources)
    //                    .build());
    //
    //            // Xóa các chunk sau khi hợp nhất thành công
    //            deleteUploadedChunks(bucketName, chunkNames);
    //
    //            return generateFileUrl(fileName);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            // Nếu có lỗi, xóa các chunk đã upload
    //            deleteUploadedChunks(bucketName, chunkNames);
    //            return "Error uploading file in chunks";
    //        }
    //    }

    private void deleteUploadedChunks(String bucketName, List<String> chunkNames) {
        for (String chunkName : chunkNames) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(chunkName)
                        .build());
                System.out.println("Deleted chunk: " + chunkName);
            } catch (Exception ex) {
                System.err.println("Failed to delete chunk: " + chunkName);
                ex.printStackTrace();
            }
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public ChunkInfo getTotalChunks(String fileName) {
        try {
            String bucketName = minioProperties.getBucketName();

            // Lấy thông tin file từ MinIO
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(fileName).build());

            long fileSize = stat.size(); // Kích thước file
            int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE); // Tính tổng số chunk

            // Tính kích thước chunk cuối cùng
            long lastChunkSize = fileSize % CHUNK_SIZE;
            if (lastChunkSize == 0 && fileSize > 0) {
                lastChunkSize = CHUNK_SIZE; // Nếu chia hết, chunk cuối cùng bằng CHUNK_SIZE
            }

            // Trả về thông tin tổng số chunk, kích thước mỗi chunk, và kích thước chunk cuối cùng
            return new ChunkInfo(totalChunks, CHUNK_SIZE, lastChunkSize);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting file metadata");
        }
    }

    public FileChunkResponse getFileChunk(String fileName, int chunkNumber) {
        try {
            String bucketName = minioProperties.getBucketName();
            long startRange = (long) chunkNumber * CHUNK_SIZE;
            long endRange = startRange + CHUNK_SIZE - 1;

            // Lấy thông tin object từ MinIO
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(fileName).build());

            long fileSize = stat.size();
            long contentLength = Math.min(CHUNK_SIZE, fileSize - startRange);

            // Tải một chunk của file từ MinIO
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .offset(startRange)
                    .length(contentLength)
                    .build());

            return new FileChunkResponse(inputStream, contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting file chunk", e);
        }
    }

    public void uploadChunkToMinio(String fileName, int chunkNumber, MultipartFile chunk) throws Exception {

        String chunkObjectName = "chunk_" + chunkNumber + "_" + fileName;

        try (InputStream inputStream = chunk.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioProperties.getBucketName()).object(chunkObjectName).stream(
                                    inputStream, chunk.getSize(), -1)
                            .contentType(chunk.getContentType())
                            .build());
            System.out.println("Uploaded chunk " + chunkNumber + " to MinIO as " + chunkObjectName);
        }
    }

    public String mergeChunksOnMinio(String fileName, int totalChunks) throws Exception {
        List<ComposeSource> sources = new ArrayList<>();
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;
        for (int i = 0; i < totalChunks; i++) {
            sources.add(ComposeSource.builder()
                    .bucket(minioProperties.getBucketName())
                    .object("chunk_" + i + "_" + fileName)
                    .build());
        }

        // Merge all chunks into a single object
        ObjectWriteResponse result = minioClient.composeObject(ComposeObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(uniqueFileName)
                .sources(sources)
                .build());
        System.out.println("Merged file " + fileName + " on MinIO");
        String urlFile = generateFileUrl(result.object());
        // Cleanup: Delete all chunks
        for (ComposeSource source : sources) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(source.object())
                    .build());
        }
        System.out.println("Deleted all chunks for file " + fileName);
        return urlFile;
    }

    public Map<String, Object> handleChunkUpload(
            String fileName, int chunkNumber, int totalChunks, MultipartFile chunk) {
        try {
            // Upload chunk lên MinIO
            uploadChunkToMinio(fileName, chunkNumber, chunk);

            // Nếu là chunk cuối cùng, ghép các chunk lại
            if (chunkNumber == totalChunks - 1) {
                String urlFile = mergeChunksOnMinio(fileName, totalChunks);
                return Map.of(
                        "success_merge", true, "message", "File uploaded and merged successfully", "urlFile", urlFile);
            }

            // Chunk được tải lên thành công
            return Map.of("success", true, "message", "Chunk uploaded successfully");
        } catch (Exception e) {
            // Xử lý lỗi
            e.printStackTrace();
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
