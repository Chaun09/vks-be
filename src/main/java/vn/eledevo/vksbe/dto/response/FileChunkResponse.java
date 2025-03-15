package vn.eledevo.vksbe.dto.response;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class FileChunkResponse {
    InputStream inputStream;
    long contentLength;
}
