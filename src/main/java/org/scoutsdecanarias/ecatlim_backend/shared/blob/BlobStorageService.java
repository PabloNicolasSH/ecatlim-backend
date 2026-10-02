package org.scoutsdecanarias.ecatlim_backend.shared.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class BlobStorageService {

    private final BlobContainerClient container;

    @Autowired
    public BlobStorageService(BlobContainerClient container){
        this.container = container;
    }

    public UploadResponse upload(MultipartFile file, BlobDirectory directory, String fileName) throws IOException {
        if (directory.hasThumbnail() && (file.getContentType() == null || !file.getContentType().startsWith("image/"))) {
            throw new IllegalArgumentException("It's necessary an image for this directory: " + directory.name());
        }

        String originalPath = directory.getPath() + "/" + fileName;
        BlobClient originalClient = container.getBlobClient(originalPath);

        try (var is = file.getInputStream()) {
            originalClient.upload(is, file.getSize(), true);
        }

        String thumbnailUrl = null;

        if (directory.hasThumbnail()) {
            thumbnailUrl = uploadThumbnail(file, directory, fileName);
        }

        String cleanOriginalUrl = URLDecoder.decode(originalClient.getBlobUrl(), StandardCharsets.UTF_8);

        return new UploadResponse(cleanOriginalUrl, thumbnailUrl, fileName);
    }

    public void delete(String fileName, BlobDirectory directory) {
        String originalPath = directory.getPath() + "/" + fileName;
        container.getBlobClient(originalPath).deleteIfExists();

        if (directory.hasThumbnail()) {
            String thumbPath = directory.getThumbnailPath() + "/" + fileName;
            container.getBlobClient(thumbPath).deleteIfExists();
        }
    }

    public byte[] download(String fileName, BlobDirectory directory) {
        String originalPath = directory.getPath() + "/" + fileName;

        return container
                .getBlobClient(originalPath)
                .downloadContent()
                .toBytes();
    }

    public byte[] downloadThumbnail(String fileName, BlobDirectory directory) {
        String fullPath = directory.getThumbnailPath() + "/" + fileName;

        return container
                .getBlobClient(fullPath)
                .downloadContent()
                .toBytes();
    }

    private String uploadThumbnail(MultipartFile file, BlobDirectory directory, String fileName) throws IOException {
        String thumbPath = directory.getThumbnailPath() + "/" + fileName;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(directory.getThumbnailSize(), directory.getThumbnailSize())
                .toOutputStream(os);

        byte[] bytes = os.toByteArray();
        try (var is = new ByteArrayInputStream(bytes)) {
            BlobClient thumbClient = container.getBlobClient(thumbPath);
            thumbClient.upload(is, (long) bytes.length, true);
            return URLDecoder.decode(thumbClient.getBlobUrl(), StandardCharsets.UTF_8);
        }
    }
}
