package gamestudio.server.service.authentication;

import gamestudio.server.repository.OAuthAccountRepository;
import gamestudio.server.repository.UserRepository;
import gamestudio.server.security.oauth.AuthProvider;
import gamestudio.server.security.principal.AuthUser;
import gamestudio.server.entity.OAuthAccount;
import gamestudio.server.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class OAuthAccountService
{
    private final OAuthAccountRepository accountRepository;
    private final UserRepository userService;

    public OAuthAccountService(OAuthAccountRepository accountRepository, UserRepository userService)
    {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    @Transactional
    public AuthUser findOrCreate(String id, AuthProvider provider, String usernameCandidate, String email)
    {
        OAuthAccount account = accountRepository.findByProvidedUserId(provider, id);
        if (account == null)
        {
            String username = usernameCandidate;
            while (userService.isTaken(username))
            {
                Random rnd = new Random();
                username = usernameCandidate + rnd.nextInt(10, 999);
            }

            User user = userService.save(new User(username, null, new Date()));
            accountRepository.save(new OAuthAccount(user, provider, id, email));
            return new AuthUser(user.getIdent(), user.getUsername());
        }

        User user = account.getUser();
        return new AuthUser(user.getIdent(), user.getUsername());
    }

}
