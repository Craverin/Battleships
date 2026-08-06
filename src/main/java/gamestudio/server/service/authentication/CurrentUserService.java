package gamestudio.server.service.authentication;

import gamestudio.server.security.principal.ApplicationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class CurrentUserService
{
    public ApplicationPrincipal getCurrentAuthUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not logged in");

        return (ApplicationPrincipal) authentication.getPrincipal();
    }

    public Optional<ApplicationPrincipal> getCurrentAuthUserOptional()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            return Optional.empty();

        ApplicationPrincipal applicationPrincipal = (ApplicationPrincipal) authentication.getPrincipal();

        if (applicationPrincipal == null)
            return Optional.empty();

        return Optional.of(applicationPrincipal);
    }

    public int getCurrentUserId() { return getCurrentAuthUser().userId(); }

    public String getCurrentUsername() { return getCurrentAuthUser().username(); }
}
