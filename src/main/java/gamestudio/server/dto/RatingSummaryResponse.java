package gamestudio.server.dto;

import java.util.List;

public record RatingSummaryResponse(float averageRating,
                                    int totalRatings,
                                    List<RatingDistributionResponse> ratingDistribution) { }
