package gamestudio.entity;

import gamestudio.service.exception.RatingException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;

import java.util.Date;

@Entity
@NamedQuery(name = "Rating.getAverageRating",
            query = "SELECT avg(r.rating) FROM Rating r WHERE r.game=:game")
@NamedQuery(name = "Rating.getRating",
            query = "SELECT r.rating FROM Rating r WHERE r.game=:game AND r.player=:player")
@NamedQuery(name = "Rating.getRatingCount",
            query = "SELECT count(r.rating) FROM Rating r WHERE r.game=:game")
public class Rating
{
    @Id
    @GeneratedValue
    private int ident;

    private String player;
    private String game;
    private int rating;
    private Date ratedOn;

    public Rating() {}

    public Rating(String player, String game, int rating, Date ratedOn)
    {
       this.player = player.trim();
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
