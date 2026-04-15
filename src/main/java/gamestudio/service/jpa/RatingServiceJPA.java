package gamestudio.service.jpa;

import gamestudio.entity.Rating;
import gamestudio.service.RatingService;
import gamestudio.service.exception.RatingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public class RatingServiceJPA implements RatingService
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(Rating rating) throws RatingException
    {
        List<Rating> ratings = entityManager.createQuery(
                "SELECT r FROM Rating r WHERE r.game=:game AND r.player=:player",
                    Rating.class
        )
        .setParameter("game", rating.getGame())
        .setParameter("player", rating.getPlayer())
        .getResultList();

        if (ratings.isEmpty()) entityManager.persist(rating);
        else
        {
            Rating existingRating = ratings.get(0);
            existingRating.setRating(rating.getRating());
            existingRating.setRatedOn(rating.getRatedOn());
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException
    {
        Double averageRating = entityManager.createNamedQuery("Rating.getAverageRating", Double.class)
                .setParameter("game", game).getSingleResult();

        return averageRating == null ? -1 : (int)Math.round(averageRating);
    }

    @Override
    public int getRating(String game, String player) throws RatingException
    {
        List<Integer> rating = entityManager.createNamedQuery("Rating.getRating", Integer.class)
                    .setParameter("game", game).setParameter("player", player).getResultList();

        return rating.isEmpty() ? -1 : rating.get(0);
    }

    @Override
    public int getRatingCount(String game) throws RatingException
    {
        return Math.toIntExact(entityManager.createNamedQuery("Rating.getRatingCount", Long.class)
                .setParameter("game", game).getSingleResult());
    }

    @Override
    public void reset() throws RatingException
    {
        entityManager.createNativeQuery("DELETE FROM Rating").executeUpdate();
    }
}
