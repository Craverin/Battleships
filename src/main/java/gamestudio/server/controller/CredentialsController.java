package gamestudio.server.controller;

import gamestudio.server.dto.ChangePasswordRequest;
import gamestudio.server.dto.ChangeUsernameRequest;
import gamestudio.server.dto.authentication.UserResponse;
import gamestudio.server.service.authentication.CredentialsService;
import gamestudio.server.service.authentication.CurrentUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class CredentialsController
{
    private final CredentialsService credentialsService;
    private final CurrentUserService currentUserService;

    public CredentialsController(CredentialsService credentialsService, CurrentUserService currentUserService)
    {
        this.credentialsService = credentialsService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/username")
    public UserResponse changeUsername(@RequestBody ChangeUsernameRequest request)
    {
        credentialsService.changeUsername(request.username());
        System.out.println(new UserResponse(currentUserService.getCurrentUserId(), request.username()));
        return new UserResponse(currentUserService.getCurrentUserId(), request.username());
    }

    @PostMapping("/password")
    public void changePassword(@RequestBody ChangePasswordRequest passwordRequest)
    {
        credentialsService.changePassword(passwordRequest);
    }

}
