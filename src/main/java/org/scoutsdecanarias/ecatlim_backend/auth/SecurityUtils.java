package org.scoutsdecanarias.ecatlim_backend.auth;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static String getLoggedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
