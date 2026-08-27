package gamestudio;

import gamestudio.server.dto.ChangePasswordRequest;
import gamestudio.server.entity.User;
import gamestudio.server.repository.UserRepository;
import gamestudio.server.service.authentication.CredentialsService;
import gamestudio.server.service.authentication.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest
{
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialsService credentialsService;

    @Test
    void changeUsername_validUsername_trimsAndUpdatesCurrentUser()
    {
        User user = user(7, "oldName", "hash");
        when(userRepository.findByUsername("newName")).thenReturn(Optional.empty());
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));

        credentialsService.changeUsername("  newName  ");

        assertEquals("newName", user.getUsername());
        verify(userRepository).findByUsername("newName");
    }

    @Test
    void changeUsername_tooShort_throwsIllegalStateException()
    {
        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changeUsername("abc")
        );

        verifyNoInteractions(currentUserService);
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    void changeUsername_alreadyExists_throwsIllegalStateException()
    {
        when(userRepository.findByUsername("alice")).thenReturn(
                Optional.of(user(1, "alice", "hash"))
        );

        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changeUsername("alice")
        );

        verifyNoInteractions(currentUserService);
    }

    @Test
    void changeUsername_currentUserNotFound_throwsIllegalStateException()
    {
        when(userRepository.findByUsername("newName")).thenReturn(Optional.empty());
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changeUsername("newName")
        );
    }

    @Test
    void changePassword_validCurrentPassword_updatesHash()
    {
        User user = user(7, "alice", "oldHash");
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHash");

        credentialsService.changePassword(
                new ChangePasswordRequest("oldPassword", "newPassword123")
        );

        assertEquals("newHash", user.getPasswordHash());
        verify(passwordEncoder).matches("oldPassword", "oldHash");
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsAndDoesNotChangeHash()
    {
        User user = user(7, "alice", "oldHash");
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "oldHash")).thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changePassword(
                        new ChangePasswordRequest("wrongPassword", "newPassword123")
                )
        );

        assertEquals("oldHash", user.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_missingCurrentPasswordForPasswordProtectedUser_throws()
    {
        User user = user(7, "alice", "oldHash");
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));

        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changePassword(
                        new ChangePasswordRequest(null, "newPassword123")
                )
        );

        assertEquals("oldHash", user.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_passwordlessAccountWithEmptyCurrentPassword_setsPassword()
    {
        User user = user(7, "alice", null);
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHash");

        credentialsService.changePassword(new ChangePasswordRequest("", "newPassword123"));

        assertEquals("newHash", user.getPasswordHash());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void changePassword_shortNewPassword_throwsBeforeLoadingUser()
    {
        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changePassword(
                        new ChangePasswordRequest("oldPassword", "short")
                )
        );

        verifyNoInteractions(currentUserService, userRepository, passwordEncoder);
    }

    @Test
    void changePassword_currentUserNotFound_throwsIllegalStateException()
    {
        when(currentUserService.getCurrentUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> credentialsService.changePassword(
                        new ChangePasswordRequest("oldPassword", "newPassword123")
                )
        );

        verifyNoInteractions(passwordEncoder);
    }

    private User user(int id, String username, String passwordHash)
    {
        User user = new User(username, passwordHash, new Date());
        user.setIdent(id);
        return user;
    }
}
