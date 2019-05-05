package adoctor.application.proposal.proposers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class DWProposer extends MethodSmellProposer {
    private static final String RELEASE = "release";

    @Override
    public ASTRewrite computeProposal(MethodSmell methodSmell) {
        if (methodSmell == null) {
            return null;
        }
        if (!(methodSmell instanceof DWSmell)) {
            return null;
        }
        DWSmell dwSmell = (DWSmell) methodSmell;
        MethodDeclaration smellyMethodDecl = dwSmell.getMethod().getMethodDecl();
        if (smellyMethodDecl == null) {
            return null;
        }
        Expression acquireExpr = dwSmell.getAcquireExpression();
        if (acquireExpr == null) {
            return null;
        }
        AST targetAST = smellyMethodDecl.getAST();

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
