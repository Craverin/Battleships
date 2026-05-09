package gamestudio.server.service.jpa;

import gamestudio.server.dto.RatingDistribution;
import gamestudio.server.dto.RatingDistributionResponse;
import gamestudio.server.dto.RatingSummaryResponse;
import gamestudio.server.entity.Rating;
import gamestudio.server.service.RatingService;
import gamestudio.server.service.exception.RatingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
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
    public float getAverageRating(String game) throws RatingException
    {
        Double averageRating = entityManager.createNamedQuery("Rating.getAverageRating", Double.class)
                .setParameter("game", game).getSingleResult();

        return averageRating == null ? -1 : averageRating.floatValue();
    }

    @Override
    public RatingSummaryResponse getRatingSummary(String game) throws RatingException
    {
        float averageRating = getAverageRating(game);
        int ratingCount = getRatingCount(game);

        List<RatingDistribution> distributions = entityManager.createNamedQuery("Rating.getRatingDistribution", RatingDistribution.class)
            .setParameter("game", game).getResultList();

        List<RatingDistributionResponse> distributionResponses = new ArrayList<>();

        for (RatingDistribution distribution : distributions)
        {
            System.out.println(distribution);
            int percent = ratingCount == 0 ? 0 : (int)Math.round((distribution.count() * 100.0) / ratingCount);
            distributionResponses.add(new RatingDistributionResponse(distribution.rating(),
                                                                     distribution.count(),
                                                                     percent));
        }

        return new RatingSummaryResponse(averageRating, ratingCount, distributionResponses);
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
