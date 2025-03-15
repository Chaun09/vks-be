package vn.eledevo.vksbe.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileInfoResponse {
    String fileName;
    int totalChunks;
    long chunkSize; // Kích thước mỗi chunk
    long lastChunkSize;
}
