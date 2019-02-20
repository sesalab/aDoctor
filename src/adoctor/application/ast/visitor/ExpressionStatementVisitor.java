package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;

import java.util.ArrayList;

public class ExpressionStatementVisitor extends ASTVisitor {

    private ArrayList<ExpressionStatement> statementList;

    public ExpressionStatementVisitor(ArrayList<ExpressionStatement> statementList) {
        this.statementList = statementList;
    }

    public boolean visit(ExpressionStatement statementNode) {
        statementList.add(statementNode);
        return true;
    }
}
