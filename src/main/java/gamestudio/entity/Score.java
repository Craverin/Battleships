package gamestudio.entity;

import gamestudio.repository.exception.ScoreException;

import java.util.Date;

public class Score
{
    private String game;

    private String player;

    private int points;

    private Date playedOn;

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

}
