package gamestudio.server.controller;

import gamestudio.server.dto.authentication.AuthRequest;
import gamestudio.server.dto.authentication.UserResponse;
import gamestudio.server.service.authentication.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public UserResponse register(@RequestBody AuthRequest request)
    {
        return authService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody AuthRequest request, HttpServletRequest httpRequest)
    {
        return authService.login(request, httpRequest);
    }

    @GetMapping("/me")
    public UserResponse me()
    {
        return authService.getCurrentUser();
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest httpRequest)
    {
        authService.logout(httpRequest);
    }
}
