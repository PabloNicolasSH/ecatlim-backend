package org.scoutsdecanarias.ecatlim_backend.shared.email;

public record EmailAttachment(String fileName, byte[] content, String mimeType) {
}
