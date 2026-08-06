package gamestudio.server.security.oauth;

import gamestudio.server.security.principal.ApplicationOidcUser;
import gamestudio.server.security.principal.AuthUser;
import gamestudio.server.service.authentication.OAuthAccountService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class ApplicationOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser>
{
    private final OidcUserService oidcUserService = new OidcUserService();
    private final OAuthAccountService accountService;

    public ApplicationOidcUserService(OAuthAccountService accountService)
    {
        this.accountService = accountService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
    {
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);

        String id = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String usernameCandidate = email.split("@")[0];

        AuthUser user = accountService.findOrCreate(id, AuthProvider.GOOGLE, usernameCandidate, email);

        return new ApplicationOidcUser(
                oidcUser,
                user.userId(),
                user.username()
        );
    }
}
