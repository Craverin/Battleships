package gamestudio.server.service.authentication;

import gamestudio.server.dto.ChangePasswordRequest;
import gamestudio.server.entity.User;
import gamestudio.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CredentialsService
{
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public CredentialsService(UserRepository userRepository,
                              CurrentUserService currentUserService,
                              PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public void changeUsername(String username)
    {
        if (username == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username");

        username = username.trim();

        if (username.length() < 4)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username too short");

        if (userRepository.findByUsername(username).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");

        int userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        );

        user.setUsername(username);
    }

    public void changePassword(ChangePasswordRequest passwordRequest)
    {
        String currentPassword = passwordRequest.currentPassword();
        String newPassword = passwordRequest.newPassword();

        if (newPassword == null || newPassword.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password too short");

        int userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        );

        String storedPasswordHash = user.getPasswordHash();

        if (storedPasswordHash == null)
        {
            if (currentPassword != null && !currentPassword.isEmpty())
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");

            user.setPasswordHash(passwordEncoder.encode(newPassword));
            return;
        }

        if (!passwordEncoder.matches(currentPassword, storedPasswordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");

        if (passwordEncoder.matches(newPassword, storedPasswordHash))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "New password is the same as old password");

        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }
}
