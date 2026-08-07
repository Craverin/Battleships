package gamestudio.server.service.authentication;

import gamestudio.server.dto.ChangePasswordRequest;
import gamestudio.server.entity.User;
import gamestudio.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        username = username.trim();

        if (username.length() < 4)
            throw new IllegalStateException("Username too short");

        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalStateException("Username already exists");

        int userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        user.setUsername(username);
    }

    public void changePassword(ChangePasswordRequest passwordRequest)
    {
        String currentPassword = passwordRequest.currentPassword();
        String newPassword = passwordRequest.newPassword();


        if (newPassword == null || newPassword.length() < 8)
            throw new IllegalStateException("Password too short");

        int userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        String storedPasswordHash = user.getPasswordHash();
        String newPasswordHash = passwordEncoder.encode(newPassword);

        if (currentPassword.isEmpty() && storedPasswordHash == null)
        {
            user.setPasswordHash(newPasswordHash);
            return;
        }

        System.out.println(newPassword);
        if (passwordEncoder.matches(newPassword, storedPasswordHash))
            throw new IllegalStateException("Incorrect password");

        user.setPasswordHash(newPasswordHash);
    }
}
