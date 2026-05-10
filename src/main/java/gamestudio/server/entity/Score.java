package gamestudio.server.entity;

import gamestudio.server.service.exception.ScoreException;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQuery(name = "Score.getTopScores",
            query = "SELECT s FROM Score s WHERE s.game=:game ORDER BY s.points DESC")
@NamedQuery(name = "Score.getTopScore",
            query = "SELECT max(s.points) FROM Score s WHERE s.game=:game AND s.player=:player")
public class Score
{
    @Id
    @GeneratedValue
    private int ident;

    private String game;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String player;
    private int points;
    private Date playedOn;

    public Score() {}

    public Score(String player, String game, int points, Date playedOn)
    {
        this.game = game.trim();
        this.player = player.trim();

        if (game.isEmpty() || player.isEmpty()) throw new ScoreException("Invalid game or player");
        if (points < 0) throw new ScoreException("Invalid score");

        this.points = points;
        this.playedOn = playedOn;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getPlayedOn() {
        return playedOn;
    }

    public void setPlayedOn(Date playedOn) {
        this.playedOn = playedOn;
    }

    @Override
    public String toString()
    {
        return "Score{" +
                "game='" + game + '\'' +
                ", player='" + player + '\'' +
                ", points=" + points +
                ", playedOn=" + playedOn +
                '}';
    }

    public int getIdent() { return ident; }

    public void setIdent(int ident) { this.ident = ident; }
}
