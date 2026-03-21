package gamestudio.entity;

import gamestudio.repository.exception.RatingException;

import java.util.Date;

public class Rating
{
   private String player;
   private String game;
   private int rating;
   private Date ratedOn;

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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getRatedOn() {
        return ratedOn;
    }

    public void setRatedOn(Date ratedOn) {
        this.ratedOn = ratedOn;
    }
}
