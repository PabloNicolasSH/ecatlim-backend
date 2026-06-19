package org.scoutsdecanarias.ecatlim_backend.core.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
