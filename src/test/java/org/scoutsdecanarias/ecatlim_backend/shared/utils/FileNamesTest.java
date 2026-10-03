package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileNamesTest {

    @Test
    void keepsOnlyASafeExtension() {
        assertThat(FileNames.safeExtension("Guía.PDF")).isEqualTo(".pdf");
        assertThat(FileNames.safeExtension("../../user/photos/x.jpg")).isEqualTo(".jpg");
        assertThat(FileNames.safeExtension("evil.p/../hp")).isEmpty();
        assertThat(FileNames.safeExtension("noextension")).isEmpty();
        assertThat(FileNames.safeExtension(null)).isEmpty();
    }

    @Test
    void randomBlobNameNeverContainsTheOriginalName() {
        String name = FileNames.randomBlobName("../../resources/otro-recurso.pdf");
        assertThat(name).matches("^[0-9a-f-]{36}\\.pdf$");
    }
}
