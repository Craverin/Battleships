package gamestudio.server.security.oauth;

import gamestudio.server.security.principal.ApplicationOAuth2User;
import gamestudio.server.security.principal.AuthUser;
import gamestudio.server.service.authentication.OAuthAccountService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class ApplicationOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>
{
    private final DefaultOAuth2UserService oAuthUserService = new DefaultOAuth2UserService();
    private final OAuthAccountService accountService;

    public ApplicationOAuth2UserService(OAuthAccountService accountService)
    {
        this.accountService = accountService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
    {
        OAuth2User oAuthUser = oAuthUserService.loadUser(userRequest);

        Object rawId = oAuthUser.getAttribute("id");
        if (rawId == null)
            throw new IllegalStateException("Github did not return user id");

        String id = rawId.toString();
        String usernameCandidate = oAuthUser.getAttribute("login");
        String email = oAuthUser.getAttribute("email");

        AuthUser user = accountService.findOrCreate(id, AuthProvider.GITHUB, usernameCandidate, email);

        return new ApplicationOAuth2User(
                oAuthUser,
                user.userId(),
                user.username()
        );
    }
}
