package gamestudio.server.webservice;

import gamestudio.server.dto.AddCommentRequest;
import gamestudio.server.entity.Comment;
import gamestudio.server.service.CommentService;
import gamestudio.server.service.authentication.CurrentUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController
{
    private final CommentService commentService;
    public CommentController(CommentService commentService)
    {
        this.commentService = commentService;
    }

    @GetMapping("/{game}")
    public List<Comment> getComments(@PathVariable String game)
    {
        return commentService.getComments(game);
    }

    @GetMapping("/{game}/me")
    public List<Comment> getMyComments(@PathVariable String game)
    {
        return commentService.getMyComments(game);
    }

    @PostMapping("/{game}")
    public void addComment(@PathVariable String game, @RequestBody AddCommentRequest comment)
    {
        commentService.addComment(game, comment);
    }
}
