package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FileSignaturesTest {

    private static MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("file", name, "application/octet-stream", content);
    }

    @Test
    void acceptsContentThatMatchesTheExtension() {
        assertThat(FileSignatures.contentMatchesExtension(file("a.pdf", "%PDF-1.7 rest".getBytes(StandardCharsets.US_ASCII)))).isTrue();
        assertThat(FileSignatures.contentMatchesExtension(file("a.docx", new byte[]{'P', 'K', 3, 4, 0, 0}))).isTrue();
        assertThat(FileSignatures.contentMatchesExtension(file("a.PNG", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}))).isTrue();
        assertThat(FileSignatures.contentMatchesExtension(file("a.jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}))).isTrue();
    }

    @Test
    void rejectsARenamedFile() {
        assertThat(FileSignatures.contentMatchesExtension(file("fake.pdf", "<html>not a pdf</html>".getBytes(StandardCharsets.UTF_8)))).isFalse();
        assertThat(FileSignatures.contentMatchesExtension(file("fake.png", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)))).isFalse();
    }

    @Test
    void rejectsUnknownExtensionsAndEmptyFiles() {
        assertThat(FileSignatures.contentMatchesExtension(file("script.exe", new byte[]{'M', 'Z', 0}))).isFalse();
        assertThat(FileSignatures.contentMatchesExtension(file("noextension", new byte[]{'%', 'P', 'D', 'F', '-'}))).isFalse();
        assertThat(FileSignatures.contentMatchesExtension(file("empty.pdf", new byte[0]))).isFalse();
    }
}
