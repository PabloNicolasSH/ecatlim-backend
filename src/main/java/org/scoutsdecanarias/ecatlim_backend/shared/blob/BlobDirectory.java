package org.scoutsdecanarias.ecatlim_backend.shared.blob;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum BlobDirectory {
    PROFILE_PHOTOS("user/photos", "user/photos/thumbnails", 200),;

    private final String path;
    private final String thumbnailPath;
    private final int thumbnailSize;

    BlobDirectory(String path, String thumbnailPath, int thumbnailSize){
        this.path = path;
        this.thumbnailPath = thumbnailPath;
        this.thumbnailSize = thumbnailSize;
    }

    BlobDirectory(String path){
        this.path = path;
        this.thumbnailPath = null;
        this.thumbnailSize = 0;
    }

    public boolean hasThumbnail() {
        return thumbnailPath != null;
    }

    public String getThumbnailPath() {
        if (thumbnailPath == null) {
            throw new IllegalStateException(
                    "Directory " + this.name() + " does not support thumbnails"
            );
        }
        return thumbnailPath;
    }

    public int getThumbnailSize() {
        if (!hasThumbnail()) {
            throw new IllegalStateException(
                    "Directory " + this.name() + " does not support thumbnails"
            );
        }
        return thumbnailSize;
    }
}
