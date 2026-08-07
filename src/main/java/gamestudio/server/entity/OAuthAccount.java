package gamestudio.server.entity;

import gamestudio.server.security.oauth.AuthProvider;
import jakarta.persistence.*;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "UniqueProviderAndUserId",
                columnNames = {"provider", "provider_user_id"}
        )
)
@NamedQuery(name = "OAuthAccount.findByProvidedUserId",
            query = "SELECT u FROM OAuthAccount u WHERE u.provider=:provider AND u.providerUserId=:id")
public class OAuthAccount
{
    @Id
    @GeneratedValue
    private int ident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    private String email;

    public OAuthAccount() { }

    public OAuthAccount(User user, AuthProvider provider, String providerUserId, String email)
    {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
    }


    public User getUser()
    {
        return user;
    }
}
