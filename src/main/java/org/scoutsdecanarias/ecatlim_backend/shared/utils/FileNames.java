package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import java.util.Locale;
import java.util.UUID;

public final class FileNames {

    private FileNames() {}

    public static String randomBlobName(String originalFilename) {
        return UUID.randomUUID() + safeExtension(originalFilename);
    }

    public static String safeExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        String extension = dot >= 0 ? originalFilename.substring(dot + 1) : "";
        return extension.matches("[A-Za-z0-9]{1,10}") ? "." + extension.toLowerCase(Locale.ROOT) : "";
    }
}
