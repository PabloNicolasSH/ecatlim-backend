package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Checks that the content of an upload matches the format its extension claims, so a file cannot pass as a PDF,
 * Word document or image just by being renamed.
 */
public final class FileSignatures {

    private static final byte[] PDF = {'%', 'P', 'D', 'F', '-'};
    private static final byte[] ZIP = {'P', 'K', 0x03, 0x04};
    private static final byte[] OLE = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    private static final Map<String, List<byte[]>> SIGNATURES_BY_EXTENSION = Map.of(
            ".pdf", List.of(PDF),
            ".docx", List.of(ZIP),
            ".doc", List.of(OLE),
            ".jpg", List.of(JPEG),
            ".jpeg", List.of(JPEG),
            ".png", List.of(PNG));

    private FileSignatures() {}

    /** False when the extension is not one of PDF, Word or JPG/PNG, or when the content does not match it. */
    public static boolean contentMatchesExtension(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        List<byte[]> signatures = dot < 0 ? null : SIGNATURES_BY_EXTENSION.get(name.substring(dot));
        if (signatures == null) {
            return false;
        }

        byte[] header = new byte[8];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        } catch (IOException e) {
            return false;
        }
        return signatures.stream().anyMatch(signature -> read >= signature.length
                && Arrays.equals(Arrays.copyOf(header, signature.length), signature));
    }
}
