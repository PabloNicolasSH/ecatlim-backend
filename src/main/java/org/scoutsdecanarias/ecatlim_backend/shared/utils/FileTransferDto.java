package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Setter
@Accessors(chain=true)
public class FileTransferDto {
    private byte[] file;
    private String fileName;
    private MediaType mediaType;
    private ContentDisposition.Builder contentDisposition = ContentDisposition.inline();

    public FileTransferDto(byte[] file, String fileName, String mediaType) {
        this.file = file;
        this.fileName = fileName;
        this.mediaType = MediaType.parseMediaType(mediaType);
    }

    public ResponseEntity<byte[]> asResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Filename", UriUtils.encode(fileName, StandardCharsets.UTF_8));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Filename");
        headers.setContentDisposition(contentDisposition.filename(fileName).build());
        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(file);
    }
}