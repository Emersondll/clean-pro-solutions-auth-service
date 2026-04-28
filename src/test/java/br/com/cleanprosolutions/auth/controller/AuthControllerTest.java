package br.com.cleanprosolutions.auth.controller;

import br.com.cleanprosolutions.auth.dto.AuthResponse;
import br.com.cleanprosolutions.auth.dto.LoginRequest;
import br.com.cleanprosolutions.auth.dto.RefreshTokenRequest;
import br.com.cleanprosolutions.auth.dto.RegisterRequest;
import br.com.cleanprosolutions.auth.dto.TokenValidationResponse;
import br.com.cleanprosolutions.auth.enumerations.UserRole;
import br.com.cleanprosolutions.auth.exception.GlobalExceptionHandler;
import br.com.cleanprosolutions.auth.exception.InvalidCredentialsException;
import br.com.cleanprosolutions.auth.exception.UserAlreadyExistsException;
import br.com.cleanprosolutions.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer tests for {@link AuthController} using {@code @WebMvcTest}.
 *
 * <p>The service layer is fully mocked. Only HTTP handling, validation and
 * exception mapping are tested here.</p>
 */
@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class})
@DisplayName("AuthController — Web Layer Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // --- POST /auth/register ---

    @Test
    @DisplayName("shouldReturn201WhenRegistrationIsSuccessful")
    @WithMockUser
    void shouldReturn201WhenRegistrationIsSuccessful() throws Exception {
        final RegisterRequest request = new RegisterRequest("new@user.com", "password123", UserRole.CLIENT);
        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("shouldReturn409WhenEmailAlreadyExists")
    @WithMockUser
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        final RegisterRequest request = new RegisterRequest("dup@user.com", "password123", UserRole.CLIENT);
        doThrow(new UserAlreadyExistsException("dup@user.com"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("shouldReturn400WhenRegisterRequestHasInvalidEmail")
    @WithMockUser
    void shouldReturn400WhenRegisterRequestHasInvalidEmail() throws Exception {
        final RegisterRequest request = new RegisterRequest("not-an-email", "password123", UserRole.CLIENT);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- POST /auth/login ---

    @Test
    @DisplayName("shouldReturn200WithTokensWhenLoginIsSuccessful")
    @WithMockUser
    void shouldReturn200WithTokensWhenLoginIsSuccessful() throws Exception {
        final LoginRequest request = new LoginRequest("user@test.com", "password123");
        final AuthResponse response = AuthResponse.of("access.token", "refresh.token", 900L);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("shouldReturn401WhenCredentialsAreInvalid")
    @WithMockUser
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        final LoginRequest request = new LoginRequest("user@test.com", "wrongPass");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // --- POST /auth/refresh ---

    @Test
    @DisplayName("shouldReturn200WithNewTokensWhenRefreshIsSuccessful")
    @WithMockUser
    void shouldReturn200WithNewTokensWhenRefreshIsSuccessful() throws Exception {
        final RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");
        final AuthResponse response = AuthResponse.of("new.access.token", "new.refresh.token", 900L);
        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"));
    }

    // --- GET /auth/validate ---

    @Test
    @DisplayName("shouldReturn200WithValidationResultWhenTokenIsValid")
    @WithMockUser
    void shouldReturn200WithValidationResultWhenTokenIsValid() throws Exception {
        final TokenValidationResponse validation =
                new TokenValidationResponse(true, "user@test.com", "CLIENT", "usr-001");
        when(authService.validate(anyString())).thenReturn(validation);

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }
}
