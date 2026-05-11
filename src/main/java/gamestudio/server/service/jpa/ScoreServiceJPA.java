package gamestudio.server.service.jpa;

import gamestudio.server.entity.Score;
import gamestudio.server.entity.User;
import gamestudio.server.service.ScoreService;
import gamestudio.server.service.authentication.CurrentUserService;
import gamestudio.server.service.exception.ScoreException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Transactional
@Service
public class ScoreServiceJPA implements ScoreService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserService currentUserService;

    public ScoreServiceJPA(CurrentUserService currentUserService)
    {
        this.currentUserService = currentUserService;
    }

    @Override
    public void addScore(String game, int userId, String username, int score) throws ScoreException
    {
        User user = entityManager.getReference(User.class, userId);

        entityManager.persist(new Score(user, game, score, new Date()));
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException
    {
        return entityManager.createNamedQuery("Score.getTopScores", Score.class)
                .setParameter("game", game).setMaxResults(10).getResultList();
    }

    @Override
    public int getMyTopScore(String game) throws ScoreException
    {
        int userId = currentUserService.getCurrentUserId();
        Integer topScore = entityManager.createNamedQuery("Score.getTopScore", Integer.class)
                .setParameter("game", game).setParameter("userId", userId)
                .setMaxResults(10).getSingleResultOrNull();

        return topScore == null ? -1 : topScore;
    }

    @Override
    public void reset() throws ScoreException
    {
        entityManager.createNativeQuery("DELETE FROM Score").executeUpdate();
    }
}
