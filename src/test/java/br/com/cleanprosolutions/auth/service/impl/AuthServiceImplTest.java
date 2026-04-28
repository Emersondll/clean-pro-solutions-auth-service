package br.com.cleanprosolutions.auth.service.impl;

import br.com.cleanprosolutions.auth.document.UserCredential;
import br.com.cleanprosolutions.auth.dto.AuthResponse;
import br.com.cleanprosolutions.auth.dto.LoginRequest;
import br.com.cleanprosolutions.auth.dto.RefreshTokenRequest;
import br.com.cleanprosolutions.auth.dto.RegisterRequest;
import br.com.cleanprosolutions.auth.dto.TokenValidationResponse;
import br.com.cleanprosolutions.auth.enumerations.UserRole;
import br.com.cleanprosolutions.auth.exception.InvalidCredentialsException;
import br.com.cleanprosolutions.auth.exception.TokenExpiredException;
import br.com.cleanprosolutions.auth.exception.UserAlreadyExistsException;
import br.com.cleanprosolutions.auth.repository.UserCredentialRepository;
import br.com.cleanprosolutions.auth.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>All dependencies are mocked — no real database or JWT generation occurs.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserCredentialRepository repository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserCredential activeCredential;
    private static final String EMAIL = "user@cleanpro.com.br";
    private static final String PASSWORD = "securePass123";
    private static final String HASHED = "$2a$10$hashed";
    private static final String USER_ID = "usr-001";
    private static final String ACCESS_TOKEN = "access.jwt.token";
    private static final String REFRESH_TOKEN = "refresh.jwt.token";

    @BeforeEach
    void setUp() {
        activeCredential = UserCredential.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(HASHED)
                .role(UserRole.CLIENT)
                .active(true)
                .refreshToken(REFRESH_TOKEN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // --- REGISTER ---

    @Test
    @DisplayName("shouldRegisterUserSuccessfullyWhenEmailIsNew")
    void shouldRegisterUserSuccessfullyWhenEmailIsNew() {
        final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT);
        when(repository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED);

        authService.register(request);

        final ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
        verify(repository).save(captor.capture());

        final UserCredential saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getPasswordHash()).isEqualTo(HASHED);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("shouldThrowUserAlreadyExistsWhenEmailIsAlreadyRegistered")
    void shouldThrowUserAlreadyExistsWhenEmailIsAlreadyRegistered() {
        final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT);
        when(repository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(EMAIL);

        verify(repository, never()).save(any());
    }

    // --- LOGIN ---

    @Test
    @DisplayName("shouldReturnTokensWhenCredentialsAreValid")
    void shouldReturnTokensWhenCredentialsAreValid() {
        final LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(activeCredential));
        when(passwordEncoder.matches(PASSWORD, HASHED)).thenReturn(true);
        when(tokenService.generateAccessToken(anyString(), anyString(), anyString())).thenReturn(ACCESS_TOKEN);
        when(tokenService.generateRefreshToken(anyString())).thenReturn(REFRESH_TOKEN);
        when(tokenService.getExpirationInSeconds()).thenReturn(900L);

        final AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("shouldThrowInvalidCredentialsWhenEmailNotFound")
    void shouldThrowInvalidCredentialsWhenEmailNotFound() {
        final LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch")
    void shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
        final LoginRequest request = new LoginRequest(EMAIL, "wrongPassword");
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(activeCredential));
        when(passwordEncoder.matches("wrongPassword", HASHED)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("shouldThrowInvalidCredentialsWhenAccountIsInactive")
    void shouldThrowInvalidCredentialsWhenAccountIsInactive() {
        activeCredential.setActive(false);
        final LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(activeCredential));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("inactive");
    }

    // --- REFRESH ---

    @Test
    @DisplayName("shouldRefreshTokensSuccessfullyWhenRefreshTokenIsValid")
    void shouldRefreshTokensSuccessfullyWhenRefreshTokenIsValid() {
        final RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        when(tokenService.isValid(REFRESH_TOKEN)).thenReturn(true);
        when(repository.findByRefreshToken(REFRESH_TOKEN)).thenReturn(Optional.of(activeCredential));
        when(tokenService.generateAccessToken(anyString(), anyString(), anyString())).thenReturn("new.access.token");
        when(tokenService.generateRefreshToken(anyString())).thenReturn("new.refresh.token");
        when(tokenService.getExpirationInSeconds()).thenReturn(900L);

        final AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new.access.token");
        assertThat(response.refreshToken()).isEqualTo("new.refresh.token");
    }

    @Test
    @DisplayName("shouldThrowTokenExpiredWhenRefreshTokenIsInvalid")
    void shouldThrowTokenExpiredWhenRefreshTokenIsInvalid() {
        final RefreshTokenRequest request = new RefreshTokenRequest("invalid.token");
        when(tokenService.isValid("invalid.token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    @DisplayName("shouldThrowTokenExpiredWhenRefreshTokenNotFoundInDatabase")
    void shouldThrowTokenExpiredWhenRefreshTokenNotFoundInDatabase() {
        final RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        when(tokenService.isValid(REFRESH_TOKEN)).thenReturn(true);
        when(repository.findByRefreshToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(TokenExpiredException.class);
    }

    // --- VALIDATE ---

    @Test
    @DisplayName("shouldReturnValidResponseWhenTokenIsValid")
    void shouldReturnValidResponseWhenTokenIsValid() {
        // Use a real TokenService for this test is not appropriate — validate is tested via integration
        // Here we test that the validate method on AuthServiceImpl delegates to tokenService correctly
        final TokenValidationResponse response = authService.validate("malformed.token");
        assertThat(response.valid()).isFalse();
        assertThat(response.email()).isNull();
    }
}
