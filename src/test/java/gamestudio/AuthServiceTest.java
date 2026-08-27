package gamestudio;

import gamestudio.server.dto.authentication.AuthRequest;
import gamestudio.server.dto.authentication.UserResponse;
import gamestudio.server.entity.User;
import gamestudio.server.repository.UserRepository;
import gamestudio.server.security.principal.AuthUser;
import gamestudio.server.service.authentication.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest
{
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void clearContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_validRequest_trimsUsernameHashesPasswordAndSavesUser()
    {
        when(userRepository.isTaken("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
        {
            User user = invocation.getArgument(0);
            user.setIdent(7);
            return user;
        });

        UserResponse response = authService.register(new AuthRequest("  alice  ", "password123"));

        assertEquals(new UserResponse(7, "alice"), response);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("alice", saved.getUsername());
        assertEquals("hash", saved.getPasswordHash());
        assertNotNull(saved.getCreatedAt());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_blankUsername_throwsBadRequest()
    {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new AuthRequest("   ", "password123"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_shortPassword_throwsBadRequest()
    {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new AuthRequest("alice", "short"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_takenUsername_throwsConflict()
    {
        when(userRepository.isTaken("alice")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new AuthRequest("alice", "password123"))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_validCredentials_authenticatesAndStoresSecurityContextInSession()
    {
        User user = user(42, "alice", "storedHash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "storedHash")).thenReturn(true);
        when(request.getSession(true)).thenReturn(session);

        UserResponse response = authService.login(new AuthRequest("alice", "password123"), request);

        assertEquals(new UserResponse(42, "alice"), response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                new AuthUser(42, "alice"),
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );

        ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(session).setAttribute(
                eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY),
                contextCaptor.capture()
        );
        assertEquals(
                new AuthUser(42, "alice"),
                contextCaptor.getValue().getAuthentication().getPrincipal()
        );
    }

    @Test
    void login_blankUsername_throwsUnauthorized()
    {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new AuthRequest("   ", "password123"), request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void login_unknownUser_throwsUnauthorized()
    {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new AuthRequest("alice", "password123"), request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized()
    {
        User user = user(42, "alice", "storedHash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "storedHash")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new AuthRequest("alice", "wrongPassword"), request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(request, never()).getSession(true);
    }

    @Test
    void getCurrentUser_authenticatedPrincipal_returnsUserResponse()
    {
        AuthUser principal = new AuthUser(9, "alice");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserResponse response = authService.getCurrentUser();

        assertEquals(new UserResponse(9, "alice"), response);
    }

    @Test
    void getCurrentUser_withoutAuthentication_throwsUnauthorized()
    {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                authService::getCurrentUser
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void logout_existingSession_invalidatesSessionAndClearsSecurityContext()
    {
        AuthUser principal = new AuthUser(9, "alice");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
        when(request.getSession(false)).thenReturn(session);

        authService.logout(request);

        verify(session).invalidate();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_withoutSession_stillClearsSecurityContext()
    {
        AuthUser principal = new AuthUser(9, "alice");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
        when(request.getSession(false)).thenReturn(null);

        authService.logout(request);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private User user(int id, String username, String passwordHash)
    {
        User user = new User(username, passwordHash, new Date());
        user.setIdent(id);
        return user;
    }
}
