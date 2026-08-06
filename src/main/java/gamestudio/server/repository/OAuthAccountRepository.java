package gamestudio.server.repository;

import gamestudio.server.entity.OAuthAccount;
import gamestudio.server.security.oauth.AuthProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthAccountRepository
{
    @PersistenceContext
    private EntityManager entityManager;

    public OAuthAccount findById(AuthProvider provider, String id)
    {
        return entityManager.createNamedQuery("OAuthAccount.findById", OAuthAccount.class)
                .setParameter("provider", provider).setParameter("id", id).getSingleResultOrNull();
    }

    public OAuthAccount save(OAuthAccount oAuthAccount)
    {
        entityManager.persist(oAuthAccount);
        return oAuthAccount;
    }

}
