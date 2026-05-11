package gamestudio.server.entity;

import gamestudio.server.dto.RatingDistribution;
import gamestudio.server.service.exception.RatingException;
import jakarta.persistence.*;

import java.util.Date;

@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "UniqueGameAndUserId",
                columnNames = {"game", "user_id"}
        )
    }
)
@Entity
@NamedQuery(name = "Rating.getAverageRating",
            query = "SELECT ROUND(avg(r.rating), 1) FROM Rating r WHERE r.game=:game")
@NamedQuery(name = "Rating.getRatingDistribution",
        query = "SELECT new gamestudio.server.dto.RatingDistribution(r.rating, COUNT(r)) FROM Rating r WHERE r.game=:game GROUP BY r.rating ORDER by r.rating DESC")
@NamedQuery(name = "Rating.getRating",
            query = "SELECT r FROM Rating r WHERE r.game=:game AND r.user.ident=:userId")
@NamedQuery(name = "Rating.getRatingCount",
            query = "SELECT count(r.rating) FROM Rating r WHERE r.game=:game")
public class Rating
{
    @Id
    @GeneratedValue
    private int ident;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String player;
    private String game;
    private int rating;
    private Date ratedOn;

    public Rating() {}

    public Rating(User user, String game, int rating, Date ratedOn)
    {
        this.user = user;
        this.player = user.getUsername();

        this.game = game.trim();
        if (player.isEmpty() || game.isEmpty()) throw new RatingException("Invalid player or game");

        this.rating = rating;
        if (rating <= 0 || rating > 5) throw new RatingException("Invalid rating");

        this.ratedOn = ratedOn;
    }


    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getRating() { return rating; }

    public void setRating(int rating) { this.rating = rating; }

    public Date getRatedOn() {
        return ratedOn;
    }

    public void setRatedOn(Date ratedOn) {
        this.ratedOn = ratedOn;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }
}
