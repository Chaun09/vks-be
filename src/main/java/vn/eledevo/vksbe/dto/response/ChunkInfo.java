package vn.eledevo.vksbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChunkInfo {
    int totalChunks; // Tổng số chunk
    long chunkSize; // Kích thước mỗi chunk
    long lastChunkSize;
}
