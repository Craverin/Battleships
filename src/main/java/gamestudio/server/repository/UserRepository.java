package gamestudio.server.repository;

import gamestudio.server.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Transactional
@Service
public class UserRepository
{
    @PersistenceContext
    private EntityManager entityManager;

    public boolean isTaken(String username)
    {
        return findByUsername(username).isPresent();
    }

    public Optional<User> findByUsername(String username)
    {
        if (username == null || username.trim().isEmpty())
            return Optional.empty();

        User user = entityManager.createNamedQuery("User.findByUsername", User.class)
                .setParameter("username", username.trim()).getSingleResultOrNull();

        return Optional.ofNullable(user);
    }

    public Optional<User> findById(int id)
    {
        User user = entityManager.createNamedQuery("User.findById", User.class)
                .setParameter("id", id).getSingleResultOrNull();

        return Optional.ofNullable(user);
    }

    public User save(User user)
    {
        entityManager.persist(user);
        return user;
    }
}
