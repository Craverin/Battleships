package gamestudio.server.service.authentication;

import gamestudio.server.dto.authentication.AuthRequest;
import gamestudio.server.repository.UserRepository;
import gamestudio.server.security.principal.ApplicationPrincipal;
import gamestudio.server.security.principal.AuthUser;
import gamestudio.server.dto.authentication.UserResponse;
import gamestudio.server.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Transactional
@Service
public class AuthService
{
    private final UserRepository userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userService, PasswordEncoder passwordEncoder)
    {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(AuthRequest request)
    {
        String username = request.username();
        String password = request.password();

        if (username == null || username.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username");

        username = username.trim();

        if (password == null || password.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too short");

        if (userService.isTaken(username))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");

        String passwordHash = passwordEncoder.encode(password);

        User user = userService.save(new User(username, passwordHash, new Date()));

        return new UserResponse(user.getIdent(), user.getUsername());
    }

    public UserResponse login(AuthRequest request, HttpServletRequest httpRequest)
    {
        String username = request.username();
        String password = request.password();

        if (username == null || username.trim().isEmpty() || password == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");

        username = username.trim();

        User user = userService.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        );

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");

        AuthUser authUser = new AuthUser(user.getIdent(), user.getUsername());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return new UserResponse(user.getIdent(), user.getUsername());
    }

    public UserResponse getCurrentUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not logged in");

        ApplicationPrincipal applicationPrincipal = (ApplicationPrincipal) authentication.getPrincipal();

        return new UserResponse(applicationPrincipal.userId(), applicationPrincipal.username());
    }

    public void logout(HttpServletRequest request)
    {
        HttpSession httpSession = request.getSession(false);

        if (httpSession != null)
            httpSession.invalidate();

        SecurityContextHolder.clearContext();
    }
}
