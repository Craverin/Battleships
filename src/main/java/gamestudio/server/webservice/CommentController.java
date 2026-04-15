package gamestudio.server.webservice;

import gamestudio.entity.Comment;
import gamestudio.service.jpa.CommentServiceJPA;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@Import(CommentServiceJPA.class)
public class CommentController
{
    private final CommentServiceJPA commentService;

    public CommentController(CommentServiceJPA commentService)
    {
        this.commentService = commentService;
    }

    @GetMapping("/{game}")
    public List<Comment> getComments(@PathVariable String game)
    {
        return commentService.getComments(game);
    }

    @GetMapping("/{game}/players/{player}")
    public List<Comment> getPlayerComments(@PathVariable String game, @PathVariable String player)
    {
        return commentService.getPlayerComments(game, player);
    }

    @PostMapping
    public void addComment(@RequestBody Comment comment)
    {
        commentService.addComment(comment);
    }
}
