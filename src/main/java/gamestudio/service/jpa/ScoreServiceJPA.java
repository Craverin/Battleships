package gamestudio.service.jpa;

import gamestudio.entity.Score;
import gamestudio.service.ScoreService;
import gamestudio.service.exception.ScoreException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public class ScoreServiceJPA implements ScoreService
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addScore(Score score) throws ScoreException
    {
        entityManager.persist(score);
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException
    {
        return entityManager.createNamedQuery("Score.getTopScores", Score.class)
                .setParameter("game", game).setMaxResults(10).getResultList();
    }

    @Override
    public int getTopScore(String game, String player) throws ScoreException
    {
        Integer topScore = entityManager.createNamedQuery("Score.getTopScore", Integer.class)
                .setParameter("game", game).setParameter("player", player)
                .setMaxResults(10).getSingleResult();

        return topScore == null ? -1 : topScore;
    }

    @Override
    public void reset() throws ScoreException
    {
        entityManager.createNativeQuery("DELETE FROM Score").executeUpdate();
    }
}
