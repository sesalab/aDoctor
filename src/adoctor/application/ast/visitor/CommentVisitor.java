package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.LineComment;

import java.util.List;

public class CommentVisitor extends ASTVisitor {

    private List<Comment> comments;

    public CommentVisitor(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public boolean visit(LineComment node) {
        comments.add(node);
        return true;
    }

    @Override
    public boolean visit(BlockComment node) {
        comments.add(node);
        return true;
    }
}
