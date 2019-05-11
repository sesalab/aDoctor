package adoctor.application.proposal.proposers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.DWSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class DWProposer extends ClassSmellProposer {
    private static final String RELEASE = "release";

    @Override
    public ASTRewrite computeProposal(ClassSmell classSmell) {
        if (classSmell == null) {
            return null;
        }
        if (!(classSmell instanceof DWSmell)) {
            return null;
        }
        DWSmell dwSmell = (DWSmell) classSmell;
        Expression acquireExpr = dwSmell.getAcquireExpression();
        if (acquireExpr == null) {
            return null;
        }
        AST targetAST = acquireExpr.getAST();

        // Creates the release() statement
        ExpressionStatement releaseStat = createReleaseStatement(targetAST, acquireExpr);

        // Add the release at the end of the acquire's block
        Block acquireBlock = ASTUtilities.getParentBlock(acquireExpr);
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        ListRewrite listRewrite = astRewrite.getListRewrite(acquireBlock, Block.STATEMENTS_PROPERTY);
        listRewrite.insertLast(releaseStat, null);
        return astRewrite;
    }

    private ExpressionStatement createReleaseStatement(AST ast, Expression acquireExpr) {
        MethodInvocation releaseMethodInvocation = ast.newMethodInvocation();
        releaseMethodInvocation.setExpression((Expression) ASTNode.copySubtree(ast, acquireExpr));
        releaseMethodInvocation.setName(ast.newSimpleName(RELEASE));
        return ast.newExpressionStatement(releaseMethodInvocation);
    }
}
