package org.scoutsdecanarias.ecatlim_backend.core.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scoutsdecanarias.ecatlim_backend.core.auth.password.PasswordRequestController;
import org.scoutsdecanarias.ecatlim_backend.core.auth.password.PasswordResetService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidationErrorResponseTest {

    private MockMvc mockMvc;
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = mock(PasswordResetService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PasswordRequestController(passwordResetService))
                .setControllerAdvice(new ExceptionControllerAdvice("8MB"))
                .build();
    }

    @Test
    void invalidRequestBodyReturns400WithFieldMessages() throws Exception {
        mockMvc.perform(post("/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\",\"newPassword\":\"short\",\"newPasswordRepeat\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ecatlimMessage", containsString("al menos una mayúscula")))
                .andExpect(jsonPath("$.ecatlimMessage", containsString("Debes repetir la nueva contraseña")));

        verify(passwordResetService, never()).resetPassword(any());
    }

    @Test
    void invalidRequestParamReturns400() throws Exception {
        mockMvc.perform(get("/password/forgot").param("email", "not-an-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ecatlimMessage", containsString("formato válido")));

        verify(passwordResetService, never()).generatePasswordResetToken(any());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/password/reset").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ecatlimMessage", containsString("formato válido")));
    }

    @Test
    void validRequestReachesTheService() throws Exception {
        mockMvc.perform(get("/password/forgot").param("email", "someone@example.com"))
                .andExpect(status().isOk());

        verify(passwordResetService).generatePasswordResetToken("someone@example.com");
    }
}
