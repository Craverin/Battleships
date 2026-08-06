package gamestudio.server.service;

import gamestudio.server.dto.RatingDistribution;
import gamestudio.server.dto.RatingDistributionResponse;
import gamestudio.server.dto.RatingSummaryResponse;
import gamestudio.server.entity.Rating;
import gamestudio.server.entity.User;
import gamestudio.server.service.authentication.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class RatingService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserService currentUserService;

    public RatingService(CurrentUserService currentUserService)
    {
        this.currentUserService = currentUserService;
    }

    public void setRating(String game, int rating)
    {
        int userId = currentUserService.getCurrentUserId();
        User user = entityManager.getReference(User.class, userId);

        List<Rating> ratings = entityManager.createNamedQuery("Rating.getRating", Rating.class)
                .setParameter("game", game).setParameter("userId", userId).getResultList();

        if (ratings.isEmpty())
            entityManager.persist(new Rating(user, game, rating, new Date()));

        else
        {
            Rating existingRating = ratings.get(0);
            existingRating.setRating(rating);
            existingRating.setRatedOn(new Date());
        }
    }

    public float getAverageRating(String game)
    {
        Double averageRating = entityManager.createNamedQuery("Rating.getAverageRating", Double.class)
                .setParameter("game", game).getSingleResult();

        return averageRating == null ? -1 : averageRating.floatValue();
    }

    public RatingSummaryResponse getRatingSummary(String game)
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

    public int getMyRating(String game)
    {
        int userId = currentUserService.getCurrentUserId();
        List<Rating> rating = entityManager.createNamedQuery("Rating.getRating", Rating.class)
                .setParameter("game", game).setParameter("userId", userId).getResultList();

        return rating.isEmpty() ? -1 : rating.get(0).getRating();
    }

    public int getRatingCount(String game)
    {
        return Math.toIntExact(entityManager.createNamedQuery("Rating.getRatingCount", Long.class)
                .setParameter("game", game).getSingleResult());
    }

    public void reset()
    {
        entityManager.createNativeQuery("DELETE FROM Rating").executeUpdate();
    }
}
