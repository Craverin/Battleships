package gamestudio.server.service.authentication;

import gamestudio.server.dto.authentication.AuthUser;
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
    public AuthUser getCurrentAuthUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not logged in");

        return (AuthUser) authentication.getPrincipal();
    }

    public Optional<AuthUser> getCurrentAuthUserOptional()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            return Optional.empty();

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        if (authUser == null)
            return Optional.empty();

        return Optional.of(authUser);
    }

    public int getCurrentUserId() { return getCurrentAuthUser().id(); }

    public String getCurrentUsername() { return getCurrentAuthUser().username(); }
}