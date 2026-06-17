package org.scoutsdecanarias.ecatlim_backend.core.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthRequest {
    private String username;
    private String password;
}
