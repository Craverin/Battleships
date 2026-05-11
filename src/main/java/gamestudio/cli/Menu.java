package gamestudio.cli;

import gamestudio.server.entity.Comment;
import gamestudio.server.entity.Score;
import gamestudio.server.service.CommentService;
import gamestudio.server.service.RatingService;
import gamestudio.server.service.ScoreService;
import org.springframework.stereotype.Component;

import java.util.List;

import static gamestudio.cli.Color.*;

@Component
public class Menu
{
    private final CommentService commentService;
    private final ScoreService scoreService;
    private final RatingService ratingService;
    private static final int PAGE_SIZE = 10;

    public Menu(CommentService commentRep, ScoreService scoreRep, RatingService ratingRep)
    {
        commentService = commentRep;
        scoreService = scoreRep;
        ratingService = ratingRep;
    }

    void showMenu()
    {
        System.out.println();
        System.out.println(ANSI_CYAN.unicode + "=== Battleships Menu ===" + ANSI_RESET.unicode);
        System.out.println(ANSI_YELLOW.unicode + "1." + ANSI_RESET.unicode + " New Game");
        System.out.println(ANSI_YELLOW.unicode + "2." + ANSI_RESET.unicode + " Comments");
        System.out.println(ANSI_YELLOW.unicode + "3." + ANSI_RESET.unicode + " Rating");
        System.out.println(ANSI_YELLOW.unicode + "4." + ANSI_RESET.unicode + " Scores");
        System.out.println(ANSI_RED.unicode + "5." + ANSI_RESET.unicode + " Exit");
        System.out.print(ANSI_GREEN.unicode + "> " + ANSI_RESET.unicode);
    }

    void showUserComments(String player)
    {
        List<Comment> comments = commentService.getMyComments("battleships");

        if (comments.isEmpty())
        {
            System.out.println(ANSI_YELLOW.unicode + "You haven't left a comment yet." + ANSI_RESET.unicode);
            return;
        }

        System.out.println(ANSI_CYAN.unicode + "\n=== Your Comments ===" + ANSI_RESET.unicode);

        for (Comment comment : comments)
            System.out.println("\"" + comment.getComment() + "\"");
    }

    int getCommentsPageCount()
    {
        int totalComments = commentService.getComments("battleships").size();
        return Math.max(1, (totalComments + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    void showCommentsPage(int pageNum)
    {
        if (pageNum <= 0) return;

        List<Comment> comments = commentService.getComments("battleships");
        if (comments == null)
        {
            System.out.println(ANSI_YELLOW.unicode + "No comments yet." + ANSI_RESET.unicode);
            return;
        }

        int start = PAGE_SIZE * (pageNum - 1);
        int end = Math.min(PAGE_SIZE * pageNum, comments.size());

        System.out.println(ANSI_CYAN.unicode + "\n=== Comments | Page " + pageNum + "/" + getCommentsPageCount() + " ===" + ANSI_RESET.unicode);

        if (start >= comments.size())
        {
            System.out.println(ANSI_YELLOW.unicode + "No comments on this page." + ANSI_RESET.unicode);
            return;
        }

        for (int i = start; i < end; i++)
        {
            Comment comment = comments.get(i);
            System.out.println(ANSI_YELLOW.unicode + (i + 1) + "." + ANSI_RESET.unicode + " "
                               + ANSI_BLUE.unicode + comment.getPlayer() + ANSI_RESET.unicode
                               + ": \"" + comment.getComment() + "\" " + ANSI_BRIGHT_BLACK.unicode
                               + "(" + comment.getCommentedOn() + ")" + ANSI_RESET.unicode
            );
        }
    }

    void showRatingPage(String player)
    {
        int userRating = ratingService.getMyRating("battleships");
        float averageRating = ratingService.getAverageRating("battleships");

        System.out.println(ANSI_CYAN.unicode + "\n=== Rating ===" + ANSI_RESET.unicode);

        if (userRating == -1) System.out.println(ANSI_YELLOW.unicode + "You haven't rated this game yet." + ANSI_RESET.unicode);
        else System.out.println("Your rating: " + ANSI_YELLOW.unicode + userRating + "/5" + ANSI_RESET.unicode);

        if (averageRating == -1) System.out.println(ANSI_YELLOW.unicode + "No ratings yet." + ANSI_RESET.unicode);
        else System.out.println("Average rating: " + ANSI_YELLOW.unicode + averageRating + "/5"
                                + ANSI_RESET.unicode + " (" + ANSI_BLUE.unicode
                                + ratingService.getRatingCount("battleships") + ANSI_RESET.unicode
                                + " votes)");
    }

    void showScoresPage(String player)
    {
        int userScore = scoreService.getMyTopScore("battleships");
        List<Score> scores = scoreService.getTopScores("battleships");

        System.out.println(ANSI_CYAN.unicode + "\n=== Scores ===" + ANSI_RESET.unicode);

        if (userScore == -1) System.out.println(ANSI_YELLOW.unicode + "You don't have a score yet." + ANSI_RESET.unicode);
        else System.out.println("Your best score: " + ANSI_YELLOW.unicode + userScore + ANSI_RESET.unicode);

        if (scores.isEmpty())
        {
            System.out.println(ANSI_YELLOW.unicode + "No scores yet." + ANSI_RESET.unicode);
            return;
        }

        else System.out.println(ANSI_CYAN.unicode + "\nAll-time Best:" + ANSI_RESET.unicode);

        for (int i = 0; i < scores.size(); i++)
        {
            Score score = scores.get(i);
            System.out.println(ANSI_YELLOW.unicode + (i + 1) + "." + ANSI_RESET.unicode + " "
                               + score.getPoints() + " " + ANSI_BLUE.unicode
                               + "(" + score.getPlayer() + ")" + ANSI_RESET.unicode
            );
        }
    }
}
