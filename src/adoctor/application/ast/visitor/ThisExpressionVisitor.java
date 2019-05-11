package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ThisExpression;

import java.util.ArrayList;

public class ThisExpressionVisitor extends ASTVisitor {

    private ArrayList<ThisExpression> thisExpressions;

    public ThisExpressionVisitor(ArrayList<ThisExpression> thisExpressions) {
        this.thisExpressions = thisExpressions;
    }

    @Override
    public boolean visit(ThisExpression thisExpression) {
        thisExpressions.add(thisExpression);
        return true;
    }
}
